alter table medicos add column usuario_id bigint;
alter table medicos add constraint fk_medicos_usuario foreign key (usuario_id) references usuarios(id);
