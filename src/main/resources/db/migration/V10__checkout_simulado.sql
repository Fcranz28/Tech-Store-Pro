ALTER TABLE pedidos
    ADD COLUMN nombre_destinatario VARCHAR(140),
    ADD COLUMN telefono_entrega VARCHAR(20),
    ADD COLUMN direccion_entrega VARCHAR(300),
    ADD COLUMN estado_pago VARCHAR(20) NOT NULL DEFAULT 'NO_PROCESADO',
    ADD CONSTRAINT pedidos_estado_pago_check CHECK (estado_pago IN ('NO_PROCESADO', 'SIMULADO'));
