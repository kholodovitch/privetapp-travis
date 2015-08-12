package com.googlecode.vkapi.domain.user;

/**
 * Builder for {@link VkUser} class
 * 
 * @author Alexey Grigorev
 * @see VkUser
 */
public class VkUserBuilder {

    private final int vkUserId;

    private String firstName;
    private String lastName;
    private String photo;
    private String avatar;
    private String bdate;
    private int sex;
    private String about;
    private boolean isDeactivated;

    private VkUserBuilder(int vkUserId) {
        this.vkUserId = vkUserId;
    }

    public static VkUserBuilder user(int vkUserId) {
        return new VkUserBuilder(vkUserId);
    }

    public VkUserBuilder addName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        return this;
    }

    public VkUserBuilder addPhoto(String photo) {
        this.photo = photo;
        return this;
    }

    public VkUserBuilder addAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public VkUserBuilder addBirthday(String bdate) {
        this.bdate = bdate;
        return this;
    }

	public VkUserBuilder setSex(int sex) {
		this.sex = sex;
        return this;
	}

	public VkUserBuilder setAbout(String about) {
		this.about = about;
        return this;
	}

	public VkUserBuilder setDeactivated(boolean isDeactivated) {
		this.isDeactivated = isDeactivated;
		return this;
	}

    public VkUser build() {
        return new VkUser(this);
    }

    public int getVkUserId() {
        return vkUserId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoto() {
        return photo;
    }

    public String getAvatar() {
		return avatar;
	}

	public String getBdate() {
        return bdate;
    }

	public int getSex() {
		return sex;
	}

	public String getAbout() {
		return about;
	}

	public boolean isDeactivated() {
		return isDeactivated;
	}
}
