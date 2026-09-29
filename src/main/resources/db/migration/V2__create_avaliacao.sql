CREATE TABLE desenvolvedora (
    id         BIGSERIAL    PRIMARY KEY,
    nome       VARCHAR(150) NOT NULL,
    pais_origem VARCHAR(100),
    site       VARCHAR(255)
);

CREATE TABLE categoria (
    id        BIGSERIAL   PRIMARY KEY,
    nome      VARCHAR(80) NOT NULL UNIQUE,
    descricao VARCHAR(255)
);

CREATE TABLE jogo (
    id               BIGSERIAL      PRIMARY KEY,
    titulo           VARCHAR(150)   NOT NULL,
    descricao        TEXT,
    preco            NUMERIC(10, 2) NOT NULL,
    data_lancamento  DATE,
    desenvolvedora_id BIGINT        NOT NULL REFERENCES desenvolvedora(id)
);

CREATE TABLE jogo_categoria (
    jogo_id      BIGINT NOT NULL REFERENCES jogo(id),
    categoria_id BIGINT NOT NULL REFERENCES categoria(id),
    PRIMARY KEY (jogo_id, categoria_id)
);

CREATE TABLE avaliacao (
    id             BIGSERIAL  PRIMARY KEY,
    usuario_id     BIGINT     NOT NULL REFERENCES usuario(id),
    jogo_id        BIGINT     NOT NULL REFERENCES jogo(id),
    nota           INT        NOT NULL CHECK (nota BETWEEN 1 AND 5),
    comentario     TEXT,
    data_avaliacao TIMESTAMP  NOT NULL DEFAULT now(),
    UNIQUE (usuario_id, jogo_id)
);
