UPDATE productos
SET imagen_url = CASE categoria
    WHEN 'TECLADOS' THEN 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_0,y_0/v1791215142/techstore-pro/catalog-contact-sheet.png'
    WHEN 'MOUSE' THEN 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_627,y_0/v1791215142/techstore-pro/catalog-contact-sheet.png'
    WHEN 'AUDIFONOS' THEN 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_627,y_627/v1791215142/techstore-pro/catalog-contact-sheet.png'
    WHEN 'AUDIO' THEN 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_0,y_627/v1791215142/techstore-pro/catalog-contact-sheet.png'
END
WHERE imagen_url = '/assets/catalog-contact-sheet.png'
  AND categoria IN ('TECLADOS', 'MOUSE', 'AUDIFONOS', 'AUDIO');
