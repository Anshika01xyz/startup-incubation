package com.startup.platform;
import com.startup.platform.util.Security;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SecurityTest {
 @Test void saltedHashVerifiesOnlyCorrectPassword() {
   String h=Security.hash("sample-password");
   assertTrue(Security.verify("sample-password",h));
   assertFalse(Security.verify("wrong-password",h));
 }
}
