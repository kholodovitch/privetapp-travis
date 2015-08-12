package com.googlecode.vkapi.convert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.googlecode.vkapi.domain.VkOAuthToken;
import com.googlecode.vkapi.domain.error.VkMethodParam;
import com.googlecode.vkapi.domain.group.VkGroup;
import com.googlecode.vkapi.domain.group.VkGroupBuilder;
import com.googlecode.vkapi.domain.message.*;
import com.googlecode.vkapi.domain.photo.VkAlbum;
import com.googlecode.vkapi.domain.photo.VkPhoto;
import com.googlecode.vkapi.domain.user.VkUser;
import com.googlecode.vkapi.domain.user.VkUserBuilder;

/**
 * Utility class for converting from {@link JsonNode} instances to vk.com domain
 * objects
 * 
 * @author Alexey Grigorev
 */
final class Convert {
	private static String[] photoSizes = new String[]{"w", "z", "y", "x", "m", "s"};
	private static String[] previewSizes = new String[]{"q", "p", "o"};
	private static String[] photoSizesBasic = new String[]{"src_xxxbig", "src_xxbig", "src_xbig", "src_big", "src", "src_small"};

    private Convert() {
    }

    public static VkOAuthToken toAuthToken(JsonNode node) {
        String accessToken = node.get("access_token").asText();
        int expiresIn = node.get("expires_in").asInt();
        int userId = node.get("user_id").asInt();
        return new VkOAuthToken(accessToken, expiresIn);
    }

    public static VkUser toVkUser(JsonNode node) {
        int vkUserId = node.get("uid").asInt();
        String firstName = safeReadText(node, "first_name");
        String lastName = safeReadText(node, "last_name");
        String photo = safeReadText(node, "photo_max_orig");
        String avatar = safeReadText(node, "photo_100");
        String about = safeReadText(node, "about");
        boolean isDeactivated = safeReadText(node, "deactivated") != null;
        int sex = node.get("sex").asInt();

        VkUserBuilder builder = VkUserBuilder.user(vkUserId).addName(firstName, lastName).setSex(sex - 1).setAbout(about).setDeactivated(isDeactivated);
        builder.addPhoto(photo);
        builder.addAvatar(avatar);

        JsonNode bdate = node.get("bdate");
        if (bdate != null) {
            // date format: "7.7.1987"
            builder.addBirthday(bdate.asText());
        }

        return builder.build();
    }

	private static String safeReadText(JsonNode node, String field) {
		return node.get(field) != null ? node.get(field).asText() : null;
	}

    public static VkPhoto toVkPhoto(JsonNode node) {
    	int photoId = node.get("pid").asInt();
        VkPhoto vkPhoto = new VkPhoto();
        vkPhoto.setPhotoId(photoId);
        vkPhoto.setUrl3ExtraBig(getPhotoUrlBasic(node, 0));
        vkPhoto.setUrl2ExtraBig(getPhotoUrlBasic(node, 1));
        vkPhoto.setUrlExtraBig(getPhotoUrlBasic(node, 2));
        vkPhoto.setUrlBig(getPhotoUrlBasic(node, 3));
        vkPhoto.setUrlMedium(getPhotoUrlBasic(node, 4));
        vkPhoto.setUrlSmall(getPhotoUrlBasic(node, 5));
		if (node.has("likes") && node.get("likes").has("count"))
			vkPhoto.setLikesCount(node.get("likes").get("count").asLong(0));
		return vkPhoto;
    }

	public static VkAlbum toVkAlbum(JsonNode node) {
		VkAlbum album = new VkAlbum();
		album.setAlbumId(node.get("aid").asLong());
		album.setName(node.get("title").asText());
		album.setCover(toVkPhotoSpecial(node));
		album.setSize(node.get("size").asInt());
		return album;
	}

	private static VkPhoto toVkPhotoSpecial(JsonNode node) {
		VkPhoto cover = new VkPhoto();
		Map<String, String> sizesMap = new HashMap<String, String>();  
		ArrayNode sizes = (ArrayNode)node.get("sizes");
		for (int i = 0; i < sizes.size(); i++) {
			JsonNode size = sizes.get(i);
			sizesMap.put(size.get("type").asText(), size.get("src").asText());
		}

		cover.setUrl3ExtraBig(getPhotoUrl(sizesMap, 0));
		cover.setUrl2ExtraBig(getPhotoUrl(sizesMap, 1));
		cover.setUrlExtraBig(getPhotoUrl(sizesMap, 2));
		cover.setUrlBig(getPhotoUrl(sizesMap, 3));
		cover.setUrlMedium(getPhotoUrl(sizesMap, 4));
		cover.setUrlSmall(getPhotoUrl(sizesMap, 5));
		cover.setUrlPreviewBig(getPhotoUrlPreview(sizesMap, 0));
		cover.setUrlPreviewMedium(getPhotoUrlPreview(sizesMap, 1));
		cover.setUrlPreviewSmall(getPhotoUrlPreview(sizesMap, 2));
		if (node.has("likes") && node.get("likes").has("count"))
			cover.setLikesCount(node.get("likes").get("count").asLong(0));
		return cover;
	}

	private static String getPhotoUrl(Map<String, String> sizesMap, int startIndex) {
		return getPhotoUrlExt(photoSizes, sizesMap, startIndex);
	}

	private static String getPhotoUrlPreview(Map<String, String> sizesMap, int startIndex) {
		return getPhotoUrlExt(previewSizes, sizesMap, startIndex);
	}

	private static String getPhotoUrlExt(String[] sizeNames, Map<String, String> sizesMap, int startIndex) {
		for (int i = startIndex; i < sizeNames.length; i++) {
			String size = sizeNames[i];
			if (sizesMap.containsKey(size)) {
				return sizesMap.get(size);
			}
		}
		return null;
	}

	private static String getPhotoUrlBasic(JsonNode node, int startIndex) {
		for (int i = startIndex; i < photoSizesBasic.length; i++) {
			JsonNode photoNode = node.get(photoSizesBasic[i]);
			if (photoNode != null)
				return photoNode.asText();
		}
		return null;
	}

    public static VkWallMessage toVkWallMessage(JsonNode node) {
        int messageId = node.get("id").asInt();
        VkWallMessageBuilder builder = VkWallMessageBuilder.message(messageId);

        int senderId = node.get("from_id").asInt();
        builder.addSender(senderId);
        int receiverId = node.get("to_id").asInt();
        builder.addReceiver(receiverId);
        builder.addDate(node.get("date").asLong());
        builder.addText(node.get("text").asText());

        JsonNode signerNode = node.get("signer_id");
        if (signerNode != null) {
            builder.addSigner(VkMessageSender.of(signerNode.asInt()));
        }

        addWallMessageAttachments(builder, node);

        return builder.build();
    }

    private static void addWallMessageAttachments(VkWallMessageBuilder builder, JsonNode node) {
        JsonNode attachmentsNode = node.get("attachments");
        if (attachmentsNode == null) {
            return;
        }

        Validate.isTrue(attachmentsNode.isArray(), "attachments is expected to be an array, got %s", attachmentsNode);
        Iterator<JsonNode> nodes = attachmentsNode.iterator();

        while (nodes.hasNext()) {
            JsonNode attachmentNode = nodes.next();
            VkAttachment attachment = toAttachment(attachmentNode);
            // TODO: get rig of null
            if (attachment != null) {
                builder.addAttachment(attachment);
            }
        }
    }

    private static VkAttachment toAttachment(JsonNode attachmentNode) {
        String typeString = attachmentNode.get("type").asText();
        VkAttachmentType type = VkAttachmentType.valueOf(typeString.toUpperCase());

        switch (type) {
            case LINK:
                return linkAttachment(attachmentNode);

        }
        // TODO: get rig of null
        return null;
    }

    private static VkLinkAttachment linkAttachment(JsonNode attachmentNode) {
        JsonNode linkNode = attachmentNode.get("link");

        String title = linkNode.get("title").asText();
        String url = linkNode.get("url").asText();

        return new VkLinkAttachment(title, url);
    }

    public static VkGroup toVkGroup(JsonNode node) {
        int gid = node.get("gid").asInt();
        VkGroupBuilder builder = VkGroupBuilder.group(gid);

        JsonNode name = node.get("name");
        if (name != null) {
            builder.addGroupName(name.asText());
        }

        JsonNode screenName = node.get("screen_name");
        if (screenName != null) {
            builder.addScreenName(screenName.asText());
        }

        JsonNode isClosed = node.get("is_closed");
        if (isClosed != null) {
            builder.setClosed(isClosed.asInt());
        }

        JsonNode type = node.get("type");
        if (type != null) {
            builder.setGroupType(type.asText());
        }

        JsonNode photo = node.get("photo");
        if (photo != null) {
            builder.addPhoto(photo.asText());
        }

        JsonNode photoMedium = node.get("photo_medium");
        if (photoMedium != null) {
            builder.addPhotoMedium(photoMedium.asText());
        }

        JsonNode photoBig = node.get("photo_big");
        if (photoBig != null) {
            builder.addPhotoBig(photoBig.asText());
        }

        JsonNode members_count = node.get("members_count");
        if (members_count != null) {
            builder.addMembersCount(members_count.asText());
        }

        JsonNode verified = node.get("verified");
        if (verified != null) {
            builder.addVerified(verified.asText());
        }

        return builder.build();
    }

    public static VkMethodParam toVkMethodParam(JsonNode node) {
        return new VkMethodParam(node.get("key").asText(), node.get("value").asText());
    }

}
