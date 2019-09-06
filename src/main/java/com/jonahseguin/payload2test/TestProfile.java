package com.jonahseguin.payload2test;

import com.jonahseguin.payload.base.PayloadCache;
import com.jonahseguin.payload.mode.profile.PayloadProfile;
import com.jonahseguin.payload.mode.profile.ProfileData;
import org.mongodb.morphia.annotations.Entity;

import java.util.UUID;

@Entity("testProfiles")
public class TestProfile extends PayloadProfile {

    public TestProfile() {
    }

    public TestProfile(String username, UUID uniqueId, String loginIp) {
        super(username, uniqueId, loginIp);
    }

    public TestProfile(ProfileData data) {
        super(data);
    }

    @Override
    public PayloadCache getCache() {
        return PayloadTest.getInstance().getCache();
    }
}
