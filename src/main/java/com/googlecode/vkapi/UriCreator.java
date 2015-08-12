package com.googlecode.vkapi;

import org.apache.commons.lang3.StringUtils;

import com.googlecode.vkapi.domain.OAuthToken;

/**
 * Creates urls for accessing VK.com api fuctions
 * 
 * @author Alexey Grigorev
 */
class UriCreator {

    private static final String METHOD_URI = "https://api.vk.com/method/";

    /**
     * Generates uri for sending authorization requests
     * 
     * @param appId id of the application
     * @param scopes needed functions (such as "friends", "messages", etc)
     * @param responseUri the uri to which the response will be sent
     * @return uri to be shown to the user to authorization
     */
    public String authUri(String appId, String[] scopes, String responseUri) {
        return "http://oauth.vk.com/authorize?" + 
                "client_id=" + appId + "&" + 
                "scope=" + StringUtils.join(scopes, ",") + "&" + 
                "redirect_uri=" + responseUri + "&" + 
                "response_type=code";
    }

    public String accessTokenUri(String appId, String appKey, String redirect_uri, String code) {
        return "https://oauth.vk.com/access_token?client_id=" + appId + "&client_secret=" + appKey +  "&redirect_uri=" + redirect_uri + "&code=" + code;
    }

    public String userInfoUri(String[] fields, OAuthToken authToken) {
        return METHOD_URI + "users.get?" + 
                "fields=" + StringUtils.join(fields, ",") + "&" + 
                "access_token=" + authToken.getAccessToken();
    }

    public String userFriendsUri(String[] fields, OAuthToken authToken) {
        return METHOD_URI + "friends.get?" + 
                "fields=" + StringUtils.join(fields, ",") + "&" + 
                "access_token=" + authToken.getAccessToken();
    }
    
    public String userGroupsUri(String[] fields, OAuthToken authToken, int count) {
        return METHOD_URI + "groups.get?" + 
                "extended=1&" +
                "fields=" + StringUtils.join(fields, ",") + "&" +
                "count=" + count + "&" +
                "access_token=" + authToken.getAccessToken();
    }

    public String groupWallMessages(long groupId, WallFiler filter, int limit, OAuthToken authToken) {
        // minus indicates that the id belongs to a group
        return wallMessages(-groupId, filter, limit, authToken);
    }
    
    public String wallMessages(long userId, WallFiler filter, int limit, OAuthToken authToken) {
        StringBuilder builder = new StringBuilder(32);
        builder.append(METHOD_URI);
        builder.append("wall.get?");
        builder.append("owner_id=").append(userId).append("&");
        if (limit > 0) {
            builder.append("count=").append(limit);
        }
        builder.append("filter=").append(filter.filterName()).append("&");
        builder.append("access_token=").append(authToken.getAccessToken());
        return builder.toString();
    }

    public String mutualFriends(int user1Id, int user2Id, OAuthToken authToken) {
        return METHOD_URI + "friends.getMutual?" + 
                "target_uid=" + user2Id + "&" +
                "source_uid" + user1Id + "&" +
                "access_token=" + authToken.getAccessToken();
    }

    public String groupInfo(long groupId, OAuthToken authToken) {
        return METHOD_URI + "groups.getById?" + 
                "gid=" + groupId + "&" + 
                "access_token=" + authToken.getAccessToken();
    }

    public String groupUsers(long groupId, int count, int offset, OAuthToken authToken) {
        return METHOD_URI + "groups.getMembers?" + 
                "gid=" + groupId + "&" + 
                "count=" + count + "&" +
                "offset=" + offset + "&" + 
                "access_token=" + authToken.getAccessToken();
    }

    public String photosGetProfile(OAuthToken authToken) {
        return METHOD_URI + "photos.getProfile?" +  
                "extended=1&" +  
                "rev=1&" +
                "access_token=" + authToken.getAccessToken();
    }

	public String photosGetAlbums(OAuthToken authToken) {
		return METHOD_URI + "photos.getAlbums?"
				+ "need_system=1&"
				+ "need_covers=1&"
				+ "photo_sizes=1&"
				+ "access_token=" + authToken.getAccessToken();
	}

	public String photosGet(OAuthToken authToken, long album_id) {
		return METHOD_URI + "photos.get?"
				+ "album_id=" + album_id + "&"
				+ "rev=1&"
				+ "access_token=" + authToken.getAccessToken();
	}

	public String photosGetById(OAuthToken authToken, String[] indexes) {
		return METHOD_URI + "photos.getById?"
				+ "photos=" + stringJoin(indexes, ",") + "&"
				+ "access_token=" + authToken.getAccessToken();
	}

	private String stringJoin(String[] items, String join) {
		String result = "";
		for (int i = 0; i < items.length; i++) {
			if (i > 0)
				result += join;

			result += items[i];
		}
		return result;
	}
}
