package br.com.zup.edu

import io.grpc.Channel
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CarroEndpointTest(val repository: CarroRepository,val grpcClient: CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub){

    /*
    * 1. happy test(tudo ok) - ok
    * 2. quando ja existe um carro com a placa
    * 3. quando os dados de entrada sao invalidos
    * */

    @BeforeEach
    fun setup(){
        //cenario
        repository.deleteAll();
    }

    @Test
    fun `deve adicionar um novo carro`() {

        //ação
        val response = grpcClient.cadastrar(
            CarrosRequest.newBuilder()
                .setModelo("Gol")
                .setPlaca("AAA-1A12")
                .build()
        )
        //validação
        with(response) {
            assertNotNull(this.id)
            assertTrue(repository.existsById(this.id)) //efeito colateral
        }
    }

        @Test
        fun `nao deve adicionar novo carro quando quando carro ou placa ja existe`() {
            //cenario
            repository.save(Carro("GOl", "AAA-0000"))
            //acao
            val error = assertThrows<StatusRuntimeException>{
                grpcClient.cadastrar(CarrosRequest.newBuilder()
                    .setPlaca("AAA-0000")
                    .setModelo("Palio")
                    .build())
            }

            //validacao
            with(error){
                assertEquals(Status.ALREADY_EXISTS.code, status.code)
                assertEquals("Carro com placa ja existente",status.description)
            }
        }

    @Test
    fun `nao deve adicionar novo carro quando dados de entrada forem invalidos`(){

        //acao
        val error = assertThrows<StatusRuntimeException>{
            grpcClient.cadastrar(CarrosRequest.newBuilder()
                .setPlaca("")
                .setModelo("")
                .build())
        }

        //validacao
        with(error){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("dados de entrada invalid",status.description)
        }
    }

    
    @Factory
    class Clients{

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub? {
            return CarrosGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}