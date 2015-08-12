package com.googlecode.vkapi;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.googlecode.vkapi.convert.JsonConverter;
import com.googlecode.vkapi.domain.OAuthToken;
import com.googlecode.vkapi.domain.error.VkErrorResponse;
import com.googlecode.vkapi.domain.group.VkGroup;
import com.googlecode.vkapi.domain.message.VkWallMessage;
import com.googlecode.vkapi.domain.photo.VkAlbum;
import com.googlecode.vkapi.domain.photo.VkPhoto;
import com.googlecode.vkapi.domain.user.GroupUsers;
import com.googlecode.vkapi.domain.user.VkUser;
import com.googlecode.vkapi.exceptions.VkException;
import com.googlecode.vkapi.exceptions.VkExceptions;

/**
 * HttpClient based implementation for {@link VkApi}. For initializing, needs
 * properties appId, appKey - obtained from vk.com application settings and
 * responseUri to which vk.com will send code for further authentication
 * 
 * @author Alexey Grigorev
 * @see VkApi
 */
public class HttpVkApi implements VkApi {

    public static final String[] APP_SCOPES = { "friends", "wall", "groups" };
    public static final String[] USER_FIELDS = { "sex", "first_name", "photo_max_orig", "photo_100", "bdate", "about" };
    public static final String[] GROUP_FIELDS = { "verified", "members_count" };

    private UriCreator uriCreator = new UriCreator();
    private HttpClientWrapper httpClient = new HttpClientWrapper();
    private JsonConverter jsonConverter = JsonConverter.INSTANCE;

    private final String appId;
    private final String appKey;
    private final String responseUri;

    /**
     * @param appId application id
     * @param appKey application key
     * @param responseUri url for sending the code
     */
    public HttpVkApi(String appId, String appKey, String responseUri) {
        this.appId = appId;
        this.appKey = appKey;
        this.responseUri = responseUri;
    }

    @Override
    public String getAuthUri() {
        return uriCreator.authUri(appId, APP_SCOPES, responseUri);
    }

    @Override
    public OAuthToken authUser(String code) throws VkException {
        Validate.notNull(code, "Expected code not to be null");

        String accessTokenUri = uriCreator.accessTokenUri(appId, appKey, responseUri, code);
        String json = executeAndProcess(accessTokenUri, null);
        return jsonConverter.jsonToAuthToken(json);
    }

    @Override
    public VkUser currentUserInfo(OAuthToken authToken) throws VkException {
        String uri = uriCreator.userInfoUri(USER_FIELDS, authToken);
        String json = executeAndProcess(uri, authToken);
        List<VkUser> result = jsonConverter.jsonToUserList(json);
        return firstOrNull(result);
    }

    private static <E> E firstOrNull(List<E> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    private String executeAndProcess(String uri, OAuthToken authToken) throws VkException {
        String json = httpClient.executeGet(uri);

        if (StringUtils.startsWith(json, "{\"error\":")) {
        	try {
                VkErrorResponse error = jsonConverter.jsonToVkError(json);
                VkExceptions.throwAppropriate(error, authToken);				
			} catch (Exception e) {
				System.err.println(e.toString() + " [JSON: " + json);
                throw new VkException(json);
			}
        }

        return json;
    }

    @Override
    public Collection<VkUser> getFriends(OAuthToken authToken) throws VkException {
        String uri = uriCreator.userFriendsUri(USER_FIELDS, authToken);
        String json = executeAndProcess(uri, authToken);
        return jsonConverter.jsonToUserList(json);
    }

    @Override
    public Collection<VkGroup> getGroups(OAuthToken authToken, int count) throws VkException {
        String uri = uriCreator.userGroupsUri(GROUP_FIELDS, authToken, count);
        String json = executeAndProcess(uri, authToken);
        return jsonConverter.jsonToVkGroups(json);
    }

    @Override
    public Collection<VkWallMessage> lastGroupWallMessages(long groupId, WallFiler filter, OAuthToken authToken)
            throws VkException {
        return lastGroupWallMessages(groupId, filter, 0, authToken);
    }

    @Override
    public Collection<VkWallMessage> lastGroupWallMessages(long groupId, WallFiler filter, int limit,
            OAuthToken authToken) throws VkException {
        Validate.inclusiveBetween(0, 100, limit, "limit must be between 0 and 100, got %d", limit);

        String uri = uriCreator.groupWallMessages(groupId, filter, limit, authToken);
        String json = executeAndProcess(uri, authToken);

        return jsonConverter.jsonToWallMessage(json);
    }

    @Override
    public Set<Integer> mutualFriends(int user1Id, int user2Id, OAuthToken authToken) throws VkException {
        String uri = uriCreator.mutualFriends(user1Id, user2Id, authToken);
        String json = executeAndProcess(uri, authToken);
        return jsonConverter.jsonToIntegerSet(json);
    }

    @Override
    public VkGroup groupInfo(long groupId, OAuthToken authToken) throws VkException {
        String uri = uriCreator.groupInfo(groupId, authToken);
        String json = executeAndProcess(uri, authToken);
        List<VkGroup> result = jsonConverter.jsonToVkGroups(json);
        return firstOrNull(result);
    }
    
    @Override
    public Set<Integer> groupUsers(long groupId, OAuthToken authToken) throws VkException {
        int count = 1000, offset = 0;
        GroupUsers groupUsers = extractNextUsersFromGroup(groupId, authToken, count, offset);
        
        if (groupUsers.allExtracted()) {
            return groupUsers.getUsers();
        } else {
            return theRestOfUsers(groupId, authToken, count, groupUsers);
        }
    }

	@Override
	public Collection<VkPhoto> currentUserProfilePhotos(OAuthToken authToken) throws VkException {
		String uri = uriCreator.photosGetProfile(authToken);
		String json = executeAndProcess(uri, authToken);
		return jsonConverter.jsonToPhotoList(json);
	}

	@Override
	public Collection<VkAlbum> albums(OAuthToken authToken) throws VkException {
		String uri = uriCreator.photosGetAlbums(authToken);
		String json = executeAndProcess(uri, authToken);
		return jsonConverter.jsonToAlbumsList(json);
	}

	@Override
	public Collection<VkPhoto> get(OAuthToken authToken, long albumId) throws VkException {
		String uri = uriCreator.photosGet(authToken, albumId);
		String json = executeAndProcess(uri, authToken);
		return jsonConverter.jsonToPhotoList(json);
	}

	@Override
	public Collection<VkPhoto> photosGetById(OAuthToken authToken, long[] photoIds) throws VkException {
		VkUser currentUser = currentUserInfo(authToken);
		String[] photoIdStr = new String[photoIds.length];
		for (int i = 0; i < photoIdStr.length; i++)
			photoIdStr[i] = currentUser.getVkUserId() + "_" + photoIds[i];

		return photosGetById(authToken, photoIdStr);
	}

	@Override
	public Collection<VkPhoto> photosGetById(OAuthToken authToken, String[] photoIds) throws VkException {
		String uri = uriCreator.photosGetById(authToken, photoIds);
		String json = executeAndProcess(uri, authToken);
		return jsonConverter.jsonToPhotoList(json);
	}

    private GroupUsers extractNextUsersFromGroup(long groupId, OAuthToken authToken, int count, int offset)
            throws VkException {
        String uri = uriCreator.groupUsers(groupId, count, offset, authToken);
        String json = executeAndProcess(uri, authToken);
        return jsonConverter.jsonToGroupUsers(json);
    }
    
    private Set<Integer> theRestOfUsers(long groupId, OAuthToken authToken, int step, GroupUsers groupUsers)
            throws VkException {
        Set<Integer> result = new LinkedHashSet<Integer>(groupUsers.getUsers());
        int totalCount = groupUsers.getTotalCount(), extracted = step; 
        
        while (extracted < totalCount) {
            GroupUsers next = extractNextUsersFromGroup(groupId, authToken, step, extracted);
            result.addAll(next.getUsers());
            extracted = extracted + step;
        }
        
        return result;
    }

    void setUriCreator(UriCreator uriCreator) {
        this.uriCreator = uriCreator;
    }

    void setHttpClient(HttpClientWrapper httpClient) {
        this.httpClient = httpClient;
    }

    void setJsonConverter(JsonConverter jsonConverter) {
        this.jsonConverter = jsonConverter;
    }
}
