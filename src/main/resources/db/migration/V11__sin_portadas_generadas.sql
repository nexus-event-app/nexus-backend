-- Quita las portadas de noticias que son tarjetas de texto generadas automáticamente (imágenes alojadas en DEV):
-- la web muestra en su lugar sus propias fotos de portada. ImageUrlPolicy impide que vuelvan a guardarse.
update news set image_url = null
where image_url like 'https://dev.to/%'
   or image_url like 'https://%.dev.to/%'
   or image_url like 'https://forem.com/%'
   or image_url like 'https://%.forem.com/%'
   or image_url like '%dev-to-uploads%';
