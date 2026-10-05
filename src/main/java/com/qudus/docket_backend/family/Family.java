package com.qudus.docket_backend.family;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class Family {
    private String name;
    private String ownerId;
    private String id;
    private String pic;
    private long memberCount;
    private String createdAt;
    private List<FamilyMember> members;
    private List<String> memberIds;

    public Family() {}

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
        return new FamilyResponse(name, id, pic, memberCount, createdAt, members.stream().map(FamilyMember::toResponse).toList());
    }
}
