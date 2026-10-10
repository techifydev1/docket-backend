package com.qudus.docket_backend.family;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Family {
    public static final int INITIAL_KEY_VERSION = 1;

    private String name;
    private String ownerId;
    private String id;
    private String pic;
    private long memberCount;
    private String createdAt;
    private List<FamilyMember> members;
    private List<String> memberIds;
    private int keyVersion;
    private Map<String, Map<String, String>> wrappedKeys;

    public Family() {}

    public Family(String name, String ownerId, String id, String pic, long memberCount, String createdAt, List<FamilyMember> members, List<String> memberIds, int keyVersion, Map<String, Map<String, String>> wrappedKeys) {
        this.name = name;
        this.ownerId = ownerId;
        this.id = id;
        this.pic = pic;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
        this.members = members;
        this.memberIds = memberIds;
        this.keyVersion = keyVersion;
        this.wrappedKeys = wrappedKeys;
    }

    public String getName() {
        return name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getId() {
        return id;
    }

    public String getPic() {
        return pic;
    }

    public long getMemberCount() {
        return memberCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<FamilyMember> getMembers() {
        return members;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public int getKeyVersion() {
        return keyVersion;
    }

    public Map<String, Map<String, String>> getWrappedKeys() {
        return wrappedKeys;
    }

    public void addMember(String memberId, int version, String wrappedKey, FamilyMember member) {
        Map<String, Map<String, String>> keys = new HashMap<>(wrappedKeys == null ? Map.of() : wrappedKeys);
        Map<String, String> versions = new HashMap<>(keys.getOrDefault(memberId, Map.of()));
        versions.put(String.valueOf(version), wrappedKey);
        keys.put(memberId, versions);
        this.wrappedKeys = keys;

        List<FamilyMember> updatedMembers = new ArrayList<>(members == null ? List.of() : members);
        updatedMembers.add(member);
        this.members = updatedMembers;

        List<String> updatedMemberIds = new ArrayList<>(memberIds == null ? List.of() : memberIds);
        if (!updatedMemberIds.contains(memberId)) updatedMemberIds.add(memberId);
        this.memberIds = updatedMemberIds;

        this.memberCount = updatedMembers.size();
    }

    public FamilyResponse toResponse(String userId) {
        String wrappedKey = null;
        if (wrappedKeys != null) {
            Map<String, String> memberKeys = wrappedKeys.get(userId);
            if (memberKeys != null) {
                wrappedKey = memberKeys.get(String.valueOf(keyVersion));
            }
        }
        return new FamilyResponse(name, id, pic, memberCount, createdAt, members.stream().map(FamilyMember::toResponse).toList(), keyVersion, wrappedKey);
    }
}
