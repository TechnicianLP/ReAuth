package technicianlp.reauth.configuration;

import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.authentication.flows.Tokens;
import technicianlp.reauth.crypto.ProfileEncryption;

import java.util.HashMap;
import java.util.Map;

public final class ProfileBuilder {

    private final Map<String, String> profile = new HashMap<>();
    private final ProfileEncryption encryption;

    public ProfileBuilder(SessionData session, ProfileEncryption encryption) {
        this.encryption = encryption;
        this.profile.put(Profile.NAME, session.username);
        this.profile.put(Profile.UUID, session.uuid);
        encryption.saveToProfile(this.profile);
    }

    public final Profile buildMicrosoft(Tokens tokens) {
        this.profile.put(Profile.PROFILE_TYPE, Profile.PROFILE_TYPE_MICROSOFT);
        this.profile.put(Profile.XBL_TOKEN, this.encryption.encryptFieldOne(tokens.getXblToken()));
        this.profile.put(Profile.REFRESH_TOKEN, this.encryption.encryptFieldTwo(tokens.getRefreshToken()));

        return ReAuth.profiles.createProfile(this.profile);
    }

    public final Profile buildMojang(String username, String password) {
        this.profile.put(Profile.PROFILE_TYPE, Profile.PROFILE_TYPE_MOJANG);
        this.profile.put(Profile.USERNAME, this.encryption.encryptFieldOne(username));
        this.profile.put(Profile.PASSWORD, this.encryption.encryptFieldTwo(password));

        return ReAuth.profiles.createProfile(this.profile);
    }
}
