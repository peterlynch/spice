package org.sonatype.sample.fixme;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.email.SecurityEmailer;

@Component( role = SecurityEmailer.class )
public class MockSecurityEmailer
    implements SecurityEmailer
{

    public void sendForgotUsername( String email, List<String> userIds )
    {
        // TODO Auto-generated method stub

    }

    public void sendNewUserCreated( String email, String userid, String password )
    {
        // TODO Auto-generated method stub

    }

    public void sendResetPassword( String email, String password )
    {
        // TODO Auto-generated method stub

    }

}
