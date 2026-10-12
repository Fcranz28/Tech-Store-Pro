-- Authorization uses the Spring JWT and ownership checks. Direct Data API access
-- must not expose password hashes, carts, orders, or returns. The table owner
-- used by the backend retains access; non-owner roles have no RLS policies.
ALTER TABLE usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE productos ENABLE ROW LEVEL SECURITY;
ALTER TABLE carrito_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE pedidos ENABLE ROW LEVEL SECURITY;
ALTER TABLE pedido_detalles ENABLE ROW LEVEL SECURITY;
ALTER TABLE devoluciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE devolucion_detalles ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF current_schema() = 'techstore' THEN
        REVOKE ALL ON SCHEMA techstore FROM PUBLIC;
        IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
            REVOKE ALL ON SCHEMA techstore FROM anon;
            REVOKE ALL ON ALL TABLES IN SCHEMA techstore FROM anon;
            REVOKE ALL ON ALL SEQUENCES IN SCHEMA techstore FROM anon;
        END IF;
        IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
            REVOKE ALL ON SCHEMA techstore FROM authenticated;
            REVOKE ALL ON ALL TABLES IN SCHEMA techstore FROM authenticated;
            REVOKE ALL ON ALL SEQUENCES IN SCHEMA techstore FROM authenticated;
        END IF;
    END IF;
END $$;
