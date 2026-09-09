-- V2__add_document_hierarchy_fields.sql

ALTER TABLE documents
ADD COLUMN parent_document_id BIGINT,
ADD COLUMN icon VARCHAR(255),
ADD COLUMN cover_image_url VARCHAR(1024),
ADD COLUMN position_order DOUBLE PRECISION;

ALTER TABLE documents
ADD CONSTRAINT fk_documents_parent
FOREIGN KEY (parent_document_id)
REFERENCES documents(id)
ON DELETE SET NULL;
