package backend.auth.oauth.Provider;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes; // oAuth2User.getAttributes()

    // id=Bl93uN3PB1pHvkb1cncW7WZ-81nxKSp9qM-ycqOzy3g, email=yuyee@naver.com, name=한현미
    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}
