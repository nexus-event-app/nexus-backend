update guides
set content = replace(content, 'Si alguna palabra te suena a chino, búscala en nuestro [glosario](/glosario).',
                      'Si alguna palabra no la conoces, búscala en nuestro [glosario](/glosario).'),
    updated_at = current_timestamp
where slug = 'como-preparar-tu-primera-entrevista-tecnica';
