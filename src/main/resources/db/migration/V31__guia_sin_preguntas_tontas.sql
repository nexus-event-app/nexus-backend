update guides
set content = replace(content, 'y un foro donde ninguna pregunta es tonta.', 'y un foro para resolver dudas entre todas y todos.'),
    updated_at = current_timestamp
where slug = 'que-hacer-despues-de-un-bootcamp';
