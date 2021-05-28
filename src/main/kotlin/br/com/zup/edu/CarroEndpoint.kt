package br.com.zup.edu

import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.constraints.ConstraintValidator
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CarroEndpoint(@Inject val repository: CarroRepository) : CarrosGrpcServiceGrpc.CarrosGrpcServiceImplBase(){

    override fun cadastrar(request: CarrosRequest?, responseObserver: StreamObserver<CarrosResponse>?) {
        if(repository.existsByPlaca(request?.placa)){
            responseObserver?.onError(Status.ALREADY_EXISTS
                .withDescription("Carro com placa ja existente")
                .asRuntimeException())
        }
        val carro = Carro(
            modelo = request!!.modelo,
            placa = request.placa
        )

        try {
            repository.save(carro)
        }catch (e: ConstraintViolationException){
            responseObserver?.onError(Status.INVALID_ARGUMENT
                .withDescription("dados de entrada invalid")
                .asRuntimeException())
            return
        }

        responseObserver?.onNext(CarrosResponse.newBuilder().setId(carro.id!!).build())
        responseObserver?.onCompleted()
    }
}