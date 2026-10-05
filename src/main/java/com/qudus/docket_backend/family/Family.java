package com.qudus.docket_backend.family;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class Family {
    private final String name;
    private final String ownerId;
    private final String id;
    private final String pic;
    private final long memberCount;
    private final String createdAt;
    private final List<FamilyMember> members;
    private final List<String> memberIds;

    public Family(String name, String ownerId, String id, String pic, long memberCount, String createdAt, List<FamilyMember> members, List<String> memberIds) {
        this.name = name;
        this.ownerId = ownerId;
        this.id = id;
        this.pic = pic;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
        this.members = members;
        this.memberIds = memberIds;
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

    public FamilyResponse toResponse() {
        return new FamilyResponse(name, id, pic, memberCount, createdAt);
    }
}
