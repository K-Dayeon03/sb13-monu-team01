package com.project.monu.domain.useractivity.repository;

import com.project.monu.domain.useractivity.document.UserActivityDocument;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActivityMongoRepository extends MongoRepository<UserActivityDocument, UUID> {
}