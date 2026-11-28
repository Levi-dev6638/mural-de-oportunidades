CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    phone VARCHAR(255),
    institution VARCHAR(255),
    education VARCHAR(255),
    interests VARCHAR(1000),
    format VARCHAR(255),
    availability VARCHAR(255),
    courses VARCHAR(20000)
);

CREATE TABLE opportunities (
    id VARCHAR(255) PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL REFERENCES users(id),
    title VARCHAR(255),
    type VARCHAR(255),
    description VARCHAR(10000),
    company VARCHAR(255),
    area VARCHAR(255),
    subject VARCHAR(255),
    format VARCHAR(255),
    deadline VARCHAR(255),
    hours VARCHAR(255),
    location VARCHAR(255),
    link VARCHAR(255),
    requirements VARCHAR(10000),
    responsibilities VARCHAR(10000),
    internal_application BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    active BOOLEAN NOT NULL
);

CREATE TABLE applications (
    id VARCHAR(255) PRIMARY KEY,
    student_id VARCHAR(255) NOT NULL REFERENCES users(id),
    opportunity_id VARCHAR(255) NOT NULL REFERENCES opportunities(id),
    status VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE,
    resume_path VARCHAR(255),
    CONSTRAINT uk_application_student_opportunity UNIQUE (student_id, opportunity_id)
);

CREATE TABLE favorites (
    id UUID PRIMARY KEY,
    student_id VARCHAR(255) NOT NULL REFERENCES users(id),
    opportunity_id VARCHAR(255) NOT NULL REFERENCES opportunities(id),
    created_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_favorite_student_opportunity UNIQUE (student_id, opportunity_id)
);

CREATE TABLE resumes (
    id UUID PRIMARY KEY,
    student_id VARCHAR(255) NOT NULL UNIQUE REFERENCES users(id),
    original_name VARCHAR(255),
    mime_type VARCHAR(255),
    storage_path VARCHAR(255),
    uploaded_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_opportunities_owner_id ON opportunities(owner_id);
CREATE INDEX idx_applications_student_created ON applications(student_id, created_at);
CREATE INDEX idx_applications_opportunity_created ON applications(opportunity_id, created_at);
CREATE INDEX idx_favorites_student_created ON favorites(student_id, created_at);
