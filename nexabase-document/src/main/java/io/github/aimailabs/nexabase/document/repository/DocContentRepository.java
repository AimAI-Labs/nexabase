package io.github.aimailabs.nexabase.document.repository;

import io.github.aimailabs.nexabase.document.entity.DocContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocContentRepository extends MongoRepository<DocContent, Long> {
}
