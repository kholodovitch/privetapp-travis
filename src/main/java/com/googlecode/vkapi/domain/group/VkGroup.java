package com.googlecode.vkapi.domain.group;

/**
 * VK.com group with all group related information. Immutable.
 * 
 * @author Alexey Grigorev
 */
public class VkGroup {

    private final long groupId;
    private final String groupName;
    private final String screenName;
    private final boolean closed;
    private final VkGroupType groupType;
    private final String photo;
    private final String photoMedium;
    private final String photoBig;
    private final long membersCount; 
    private final boolean verified;

    public VkGroup(VkGroupBuilder vkGroupBuilder) {
        groupId = vkGroupBuilder.getGroupId();
        groupName = vkGroupBuilder.getGroupName();
        screenName = vkGroupBuilder.getScreenName();
        closed = vkGroupBuilder.isClosed();
        groupType = vkGroupBuilder.getGroupType();
        photo = vkGroupBuilder.getPhoto();
        photoMedium = vkGroupBuilder.getPhotoMedium();
        photoBig = vkGroupBuilder.getPhotoBig();
        membersCount = vkGroupBuilder.getMembersCount();
        verified = vkGroupBuilder.isVerified();
    }

    public long getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getScreenName() {
        return screenName;
    }

    public boolean isClosed() {
        return closed;
    }

    public VkGroupType getGroupType() {
        return groupType;
    }

    public String getPhoto() {
        return photo;
    }

    public String getPhotoMedium() {
        return photoMedium;
    }

    public String getPhotoBig() {
        return photoBig;
    }

    public long getMembersCount() {
		return membersCount;
	}

	public boolean isVerified() {
		return verified;
	}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof VkGroup) {
            VkGroup another = (VkGroup) obj;
            return groupId == another.groupId;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (int) (groupId ^ (groupId << 32));
    }

    @Override
    public String toString() {
        return "VkGroup [groupId=" + groupId + ", groupName=" + groupName + ", groupType=" + groupType + "]";
    }

}
