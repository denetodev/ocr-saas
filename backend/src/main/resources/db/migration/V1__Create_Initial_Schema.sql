-- Criação da tabela de usuários
-- Esta tabela armazenará informações básicas dos usuários do sistema
CREATE TABLE users (
    -- Chave primária com auto-incremento
    id BIGSERIAL PRIMARY KEY,

    -- Nome de usuário único
    username VARCHAR(50) NOT NULL UNIQUE,

    -- Email único para comunicação e recuperação de senha
    email VARCHAR(100) NOT NULL UNIQUE,

    -- Senha criptografada (nunca armazenar senhas em texto puro)
    password VARCHAR(255) NOT NULL,

    -- Papel do usuário no sistema (ROLE_USER, ROLE_ADMIN)
    role VARCHAR(20) NOT NULL,

    -- Timestamps para auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Criação da tabela de pastas
-- Esta tabela permite aos usuários organizar seus documentos em pastas
CREATE TABLE folders (
    -- Chave primária com auto-incremento
    id BIGSERIAL PRIMARY KEY,

    -- Nome da pasta
    name VARCHAR(255) NOT NULL,

    -- Referência à pasta pai (para estrutura hierárquica)
    -- Pode ser NULL para pastas de nível raiz
    parent_id BIGINT,

    -- Referência ao usuário proprietário da pasta
    user_id BIGINT NOT NULL,

    -- Timestamps para auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Chaves estrangeiras
    FOREIGN KEY (parent_id) REFERENCES folders(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Criação da tabela de documentos
-- Esta tabela armazenará os documentos processados pelo OCR
CREATE TABLE documents (
    -- Chave primária com auto-incremento
    id BIGSERIAL PRIMARY KEY,

    -- Nome do documento
    name VARCHAR(255) NOT NULL,

    -- Caminho para o arquivo original
    original_file_path VARCHAR(255) NOT NULL,

    -- Caminho para o arquivo .doc gerado
    doc_file_path VARCHAR(255),

    -- Texto extraído pelo OCR
    -- Tipo TEXT para armazenar grandes volumes de texto
    extracted_text TEXT,

    -- Status do processamento (PENDING, PROCESSING, COMPLETED, FAILED)
    status VARCHAR(20) NOT NULL,

    -- Referência à pasta onde o documento está armazenado
    -- Pode ser NULL se não estiver em nenhuma pasta
    folder_id BIGINT,

    -- Referência ao usuário proprietário do documento
    user_id BIGINT NOT NULL,

    -- Timestamps para auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Chaves estrangeiras
    FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Criação de índices para melhorar a performance das consultas

-- Índice para buscar pastas por usuário
CREATE INDEX idx_folders_user_id ON folders(user_id);

-- Índice para buscar documentos por usuário
CREATE INDEX idx_documents_user_id ON documents(user_id);

-- Índice para buscar documentos por pasta
CREATE INDEX idx_documents_folder_id ON documents(folder_id);