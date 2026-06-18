CREATE TABLE message_processing_records (
    id UUID PRIMARY KEY,
    message_id VARCHAR(255) NOT NULL UNIQUE,
    message_type VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'RECEIVED',
    error_message TEXT,
    transformation_count INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_message_id ON message_processing_records(message_id);



