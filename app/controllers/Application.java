package controllers;

import models.LocalUser;
import play.*;
import play.mvc.*;

import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import utils.ComputationUtil;
import views.html.*;

public class Application extends Controller {

    @SecureSocial.UserAwareAction
    public static Result index() {
        //ComputationUtil.AnnotationData(null);
        Logger.debug(String.valueOf(Math.abs(Integer.MIN_VALUE)));
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        LocalUser localUser = null;
        if (user != null) {
            localUser = LocalUser.find.byId(user.identityId().userId());
        }
        if (localUser != null) {
            return ok(index.render(localUser.account.userName));
        } else {
            return ok(index.render(null));
        }
    }

}
