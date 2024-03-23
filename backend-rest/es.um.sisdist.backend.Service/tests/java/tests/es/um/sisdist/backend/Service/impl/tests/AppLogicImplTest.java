/**
 *
 */
package es.um.sisdist.backend.Service.impl.tests;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.User;

/**
 * @author dsevilla
 *
 */
class AppLogicImplTest
{
        static AppLogicImpl impl;

        @BeforeAll
        static void setup()
        {
                impl = AppLogicImpl.getInstance();
        }

        @Test
        void testDefaultUser()
        {
                Optional<User> u = impl.getUserByEmail("dsevilla@um.es");
                assertEquals(u.get().getEmail(), "dsevilla@um.es");
        }
        @Test
        void testCreateUserCheckEmail()
        {
               impl.createUser("ssdd", "ssdd@um.es", "contrasena");
               Optional<User> iu = impl.getUserByEmail("ssdd@um.es");
               assertEquals(iu.get().getEmail(),"ssdd@um.es");


        }
        @Test
        void testCreateUserReturn(){
                Optional<User> e = impl.createUser("ssdd2", "ssdd@um.es", "contrasena");
                assertNotNull(e.get().getId());
        }
  
}
