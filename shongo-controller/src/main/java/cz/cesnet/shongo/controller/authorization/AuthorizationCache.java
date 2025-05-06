package cz.cesnet.shongo.controller.authorization;

import cz.cesnet.shongo.ExpirationMap;
import cz.cesnet.shongo.api.UserInformation;
import cz.cesnet.shongo.controller.acl.AclEntry;
import cz.cesnet.shongo.controller.acl.AclObjectIdentity;
import cz.cesnet.shongo.controller.api.Group;
import org.joda.time.Duration;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Represents a cache of {@link AclEntry}s
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Component
public class AuthorizationCache
{

    /**
     * Cache of {@link AclUserState} by user-id.
     */
    private ExpirationMap<String, AclUserState> aclUserStateCache = new ExpirationMap<String, AclUserState>();

    /**
     * Cache of {@link AclObjectState} by {@link AclObjectIdentity}.
     */
    private ExpirationMap<AclObjectIdentity, AclObjectState> aclObjectStateCache =
            new ExpirationMap<AclObjectIdentity, AclObjectState>();

    /**
     * @param expiration sets the {@link #aclUserStateCache} expiration
     */
    public synchronized void setAclExpiration(Duration expiration)
    {
        aclUserStateCache.setExpiration(expiration);
        aclObjectStateCache.setExpiration(expiration);
    }

    @CacheEvict({
            "userIdByAccessToken",
            "userIdByPrincipalName",
            "userDataByUserId",
            "userAuthorizationDataByAccessToken",
            "groupByGroupId",
            "groupIdByName",
            "aclEntry",
            "userIdsByGroupId"
    })
    public synchronized void clear()
    {
        aclUserStateCache.clear();
        aclObjectStateCache.clear();
    }

    @Cacheable(cacheNames = "userIdByAccessToken", key = "#accessToken")
    public String getUserIdByAccessToken(String accessToken)
    {
        return null;
    }

    @CachePut(cacheNames = "userIdByAccessToken", key = "#accessToken")
    public String putUserIdByAccessToken(String accessToken, String userId)
    {
        return userId;
    }

    @Cacheable(cacheNames = "userIdByPrincipalName", key = "#principalName")
    public String getUserIdByPrincipalName(String principalName)
    {
        return null;
    }

    @CachePut(cacheNames = "userIdByPrincipalName", key = "#principalName")
    public String putUserIdByPrincipalName(String principalName, String userId)
    {
        return userId;
    }

    @Cacheable(cacheNames = "userDataByUserId", key = "#userId")
    public UserData getUserDataByUserId(String userId)
    {
        return null;
    }

    @CachePut(cacheNames = "userDataByUserId", key = "#userId")
    public UserData putUserDataByUserId(String userId, UserData userData)
    {
        return userData;
    }

    @Cacheable(cacheNames = "userAuthorizationDataByAccessToken", key = "#accessToken")
    public UserAuthorizationData getUserAuthorizationDataByAccessToken(String accessToken)
    {
        return null;
    }

    @CachePut(cacheNames = "userAuthorizationDataByAccessToken", key = "#accessToken")
    public UserAuthorizationData putUserAuthorizationDataByAccessToken(String accessToken,
            UserAuthorizationData userAuthorizationData)
    {
        return userAuthorizationData;
    }

    @Cacheable(cacheNames = "aclEntry", key = "#aclEntryId")
    public AclEntry getAclEntryById(Long aclEntryId)
    {
        return null;
    }

    @CachePut(cacheNames = "aclEntry", key = "#aclEntry.id")
    public AclEntry putAclEntryById(AclEntry aclEntry)
    {
        return aclEntry;
    }

    @CacheEvict(cacheNames = "aclEntry", key = "#aclEntry.id")
    public void removeAclEntryById(AclEntry aclEntry)
    {
    }

    public synchronized AclUserState getAclUserStateByUserId(String userId)
    {
        return aclUserStateCache.get(userId);
    }

    public synchronized Collection<AclUserState> listAclUserStates()
    {
        return aclUserStateCache.values();
    }

    public synchronized void putAclUserStateByUserId(String userId, AclUserState aclUserState)
    {
        aclUserStateCache.put(userId, aclUserState);
    }

    public synchronized AclObjectState getAclObjectStateByIdentity(AclObjectIdentity aclObjectIdentity)
    {
        return aclObjectStateCache.get(aclObjectIdentity);
    }

    public synchronized void putAclObjectStateByIdentity(AclObjectIdentity aclObjectIdentity,
            AclObjectState aclObjectState)
    {
        aclObjectStateCache.put(aclObjectIdentity, aclObjectState);
    }

    @Cacheable(cacheNames = "groupByGroupId", key = "#groupId")
    public Group getGroupByGroupId(String groupId)
    {
        return null;
    }

    @CachePut(cacheNames = "groupByGroupId", key = "#groupId")
    public Group putGroupByGroupId(String groupId, Group group)
    {
        return group;
    }

    @Cacheable(cacheNames = "groupIdByName", key = "#groupName")
    public String getGroupIdByName(String groupName)
    {
        return null;
    }

    @CachePut(cacheNames = "groupIdByName", key = "#groupName")
    public String putGroupIdByName(String groupName, String groupId)
    {
        return groupId;
    }

    @Cacheable(cacheNames = "userIdsByGroupId", key = "#groupId")
    public UserIdSet getUserIdsInGroup(String groupId)
    {
        return null;
    }

    @CachePut(cacheNames = "userIdsByGroupId", key = "#groupId")
    public UserIdSet putUserIdsInGroup(String groupId, UserIdSet userIds)
    {
        return userIds;
    }

    @CacheEvict(cacheNames = "userIdsByGroupId", key = "#groupId")
    public void removeGroup(String groupId)
    {
    }

    @CacheEvict(cacheNames = "groupIdByName", key = "#groupName")
    public void removeGroupIdByName(String groupName)
    {
    }
}
