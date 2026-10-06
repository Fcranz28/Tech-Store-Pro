# Diseño de TechStore Pro

La tienda usa una identidad oscura de azul tecnológico, inspirada en las referencias visuales entregadas por el usuario. La portada prioriza el producto y una acción clara; el catálogo muestra fotografía, precio y disponibilidad. El panel de administración cambia a una superficie clara para facilitar lectura y tareas repetidas, con una barra lateral azul oscura. No incluye gráficos ni métricas ajenas al flujo de productos y pedidos.

## Sistema visual

- Tipografía: Manrope alojada localmente con `@fontsource/manrope`.
- Fondo de tienda: `#070b12`; superficies: `#111a27`; acento interactivo: `#246be9`.
- Formularios con etiquetas persistentes; botones con estados de carga, error y deshabilitado.
- Composición adaptable: cuadrícula de productos de cuatro, dos o dos columnas según ancho; páginas de tarea de una columna en móvil.
- Navegación visible a catálogo, carrito, pedidos y administración según la sesión.

## Recursos

- `frontend/public/assets/techstore-logo.jpg`: logo entregado por el usuario, usado en el pie de página.
- `frontend/public/assets/techstore-favicon.jpg`: símbolo entregado por el usuario, usado en favicon y cabecera.
- `frontend/public/assets/hero-headphones.png`: recurso generado con ImageGen. Prompt: “Photorealistic premium ecommerce hero product photograph, a pair of over-ear matte black wireless headphones with subtle cobalt blue reflections, floating at a three-quarter angle over a near-black studio background, soft sculpted rim light and a restrained electric blue pool of light beneath, refined materials, realistic ear cushions and metal hinges, crisp commercial photography. Product occupies the right 65% of a wide horizontal composition, generous dark negative space on the left for live web headline. No text, no logo, no extra products, no UI. Aspect ratio approximately 16:9. product-mockup.”
- `frontend/public/assets/catalog-contact-sheet.png`: recurso generado con ImageGen. Prompt: “A precise 2 by 2 product photography contact sheet for a technology accessories ecommerce catalog. Four equal square panels, each product centered independently with ample margin: top left a compact mechanical keyboard in matte black with subtle blue key backlight; top right a sculpted matte black wireless gaming mouse with one tiny cobalt light; bottom left black true wireless earbuds in an open charging case; bottom right a pair of matte black over-ear headphones. Each panel has the same neutral charcoal studio background and soft directional commercial lighting, photorealistic, premium materials, clear separation between quadrants, no overlapping objects, no text, no logos, no borders, no labels. Square overall composition. product-mockup.”

## Refinamiento editorial — octubre 2026
- Portada fotográfica oscura con titular de hasta 96 px, contraste de pesos y azul del logo.
- Catálogo sobre superficie cálida #f3f2ee, dos columnas amplias y navegación lateral. En móvil las categorías son horizontales y un resultado ocupa el ancho disponible.
- Productos sin contenedor decorativo: fotografía con radio de 14 px, nombre, descripción completa, stock y precio separados por una línea fina.
- Formularios y administración comparten tipografía, azul de interacción y espaciado. Administración conserva únicamente productos y pedidos, sin gráficos.
- Estilos existentes en frontend/src/base.css; dirección visual en frontend/src/storefront.css, importada después de la base.
- Se aplicaron las guías craft-floor y bolder de Impeccable. Filtros con aria-pressed, avisos cerrables, foco visible y respeto por movimiento reducido.
- La portada usa un titular centrado sobre la fotografía oscurecida, una acción principal clara y una franja tipográfica de marcas conocidas como referencias del sector, sin afirmar afiliaciones comerciales.
- El catálogo de demostración contiene 15 productos; los 11 añadidos en V6 usan imágenes referenciales de su categoría, indicadas en la descripción.
