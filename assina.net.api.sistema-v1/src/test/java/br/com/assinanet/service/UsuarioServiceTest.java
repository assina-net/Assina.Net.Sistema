//package br.com.assinanet.service;
//
//import br.com.assinanet.entity.Usuario;
//import br.com.assinanet.entity.enums.PerfilEnum;
//import br.com.assinanet.entity.enums.StatusEnum;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.UUID;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//
///**
// * @author Samuel Oliveira - samuk.exe@hotmail.com
// * Data: 15/07/2019 - 10:52
// */
//@DataJpaTest
//@ComponentScan("br.com.assinanet.*")
//@RunWith(SpringRunner.class)
//@TestPropertySource(locations = "classpath:application-test.properties")
//public class UsuarioServiceTest {
//
//    @Autowired
//    private UsuarioService usuarioService;
//
//    @Test
//    public void testaEncontrarPorId() {
//        Usuario usuario = usuarioService.findById(UUID.fromString("c81b3e79-1834-4c4e-bf88-47221e601c59"));
//        assertNotNull(usuario);
//        assertEquals("ADMIN", usuario.getLogin());
//    }
//
//    @Test
//    public void save() {
//
//        //id fixo por conta do teste de contrato
//        Usuario usuario = new Usuario(UUID.fromString("3877ae22-0c97-4657-acce-0ac16f5fdb45"),
//                "TESTE",
//                "teste",
//                "$2a$10$8fUY0Rf.nhePOtvg7ikoNORLgrUKX6UM8K/LIPwfHGN3TN2BPbBp6",
//                StatusEnum.ATIVO,
//                PerfilEnum.ROLE_ADMIN);
//
//        usuarioService.save(usuario);
//        usuario = usuarioService.findByLogin("teste");
//        assertNotNull(usuario);
//        assertNotNull(usuario.getId());
//        assertEquals("TESTE", usuario.getNome());
//    }
//}