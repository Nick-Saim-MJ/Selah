-- =====================================================================
-- SelahFinance · V7 · Datos semilla del sistema (catálogos globales)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Hábitos de mayordomía integral (hogar_id NULL = catálogo del sistema)
-- ---------------------------------------------------------------------
INSERT INTO habito_mayordomia (codigo, dimension, nombre, descripcion, unidad, frecuencia_meta, meta_valor, referencia_biblica, orden) VALUES
-- TIEMPO: el tiempo como don de Dios
('CULTO_PERSONAL',     'TIEMPO',  'Culto personal',                 'Tiempo de oración y lectura de la Biblia',                'MINUTOS',  'DIARIA',  30, 'Marcos 1:35',        1),
('LECCION_ES',         'TIEMPO',  'Lección de Escuela Sabática',    'Estudio diario de la lección',                             'BOOLEANO', 'DIARIA',   1, '2 Timoteo 2:15',     2),
('CULTO_FAMILIAR',     'TIEMPO',  'Culto familiar',                 'Adoración en familia',                                     'BOOLEANO', 'DIARIA',   1, 'Deuteronomio 6:6-7', 3),
('ASISTENCIA_IGLESIA', 'TIEMPO',  'Asistencia a la iglesia',        'Culto de sábado y reuniones de oración',                   'VECES',    'SEMANAL',  2, 'Hebreos 10:25',      4),
('SABADO_GUARDADO',    'TIEMPO',  'Sábado guardado',                'De puesta a puesta de sol, sin trabajo ni comercio',       'BOOLEANO', 'SEMANAL',  1, 'Éxodo 20:8-11',      5),
-- TALENTO: dones puestos al servicio
('SERVICIO_IGLESIA',   'TALENTO', 'Servicio en ministerios',        'Horas de servicio en un ministerio de la iglesia',         'HORAS',    'SEMANAL',  2, '1 Pedro 4:10',       1),
('OBRA_MISIONERA',     'TALENTO', 'Obra misionera',                 'Estudios bíblicos, visitas, literatura',                   'VECES',    'SEMANAL',  1, 'Mateo 28:19-20',     2),
('DESARROLLO_DON',     'TALENTO', 'Desarrollo de un don',           'Tiempo invertido en desarrollar una habilidad',            'HORAS',    'SEMANAL',  2, 'Mateo 25:14-30',     3),
('AYUDA_COMUNIDAD',    'TALENTO', 'Ayuda a la comunidad',           'Acción solidaria (ADRA, vecinos, necesitados)',            'VECES',    'SEMANAL',  1, 'Gálatas 6:10',       4),
-- TEMPLO: el cuerpo como templo del Espíritu (8 remedios naturales)
('AGUA',               'TEMPLO',  'Agua',                           'Vasos de agua pura al día',                                'VASOS',    'DIARIA',   8, '1 Corintios 6:19-20', 1),
('EJERCICIO',          'TEMPLO',  'Ejercicio',                      'Actividad física',                                         'MINUTOS',  'DIARIA',  30, '1 Timoteo 4:8',      2),
('DESCANSO',           'TEMPLO',  'Descanso',                       'Horas de sueño',                                           'HORAS',    'DIARIA',   7, 'Salmo 127:2',        3),
('SOL',                'TEMPLO',  'Luz solar',                      'Exposición moderada al sol',                               'MINUTOS',  'DIARIA',  15, 'Malaquías 4:2',      4),
('AIRE_PURO',          'TEMPLO',  'Aire puro',                      'Tiempo al aire libre / respiración profunda',              'MINUTOS',  'DIARIA',  15, 'Génesis 2:7',        5),
('TEMPERANCIA',        'TEMPLO',  'Temperancia',                    'Día libre de sustancias dañinas y excesos',                'BOOLEANO', 'DIARIA',   1, '1 Corintios 9:25',   6),
('ALIMENTACION',       'TEMPLO',  'Alimentación saludable',         'Comidas balanceadas a su hora',                            'VECES',    'DIARIA',   3, '1 Corintios 10:31',  7),
('CONFIANZA_DIOS',     'TEMPLO',  'Confianza en Dios',              'Momento de gratitud y entrega de preocupaciones',          'BOOLEANO', 'DIARIA',   1, 'Proverbios 3:5-6',   8),
-- TESORO: el puntaje principal se calcula desde las finanzas; estos son hábitos complementarios
('REVISION_PRESUPUESTO','TESORO', 'Revisión del presupuesto',       'Revisé mis movimientos y presupuesto de la semana',        'BOOLEANO', 'SEMANAL',  1, 'Proverbios 27:23-24', 1);

-- ---------------------------------------------------------------------
-- Destinos de ofrenda globales
-- ---------------------------------------------------------------------
INSERT INTO destino_ofrenda (hogar_id, nombre, descripcion) VALUES
(NULL, 'Presupuesto de iglesia local', 'Gastos operativos de la iglesia local'),
(NULL, 'Misiones mundiales',           'Ofrenda para la misión mundial'),
(NULL, 'Construcción de templo',       'Proyectos de construcción o mantenimiento'),
(NULL, 'Obra social / ADRA',           'Ayuda humanitaria y comunitaria'),
(NULL, 'Educación cristiana',          'Becas y colegios adventistas');

-- ---------------------------------------------------------------------
-- Banco de preguntas para la reflexión semanal
-- ---------------------------------------------------------------------
INSERT INTO pregunta_reflexion (texto, referencia_biblica, dimension) VALUES
('¿Esta semana gasté por necesidad o por impulso?',                         'Lucas 12:15',        'TESORO'),
('¿Aparté lo de Dios antes que lo mío?',                                    'Proverbios 3:9',     'TESORO'),
('¿Hubo algún gasto del que me arrepiento? ¿Qué aprendí?',                  'Proverbios 21:20',   'TESORO'),
('¿Di con alegría esta semana?',                                            '2 Corintios 9:7',    'TESORO'),
('¿Dediqué a Dios la primera hora de mis días?',                            'Salmo 5:3',          'TIEMPO'),
('¿Mi sábado fue realmente un descanso, también de las preocupaciones económicas?', 'Éxodo 20:8-11', 'TIEMPO'),
('¿Usé algún don que Dios me dio para servir a otros?',                     '1 Pedro 4:10',       'TALENTO'),
('¿A quién pude ayudar esta semana y no lo hice?',                          'Santiago 2:15-16',   'TALENTO'),
('¿Cuidé mi cuerpo como templo del Espíritu Santo?',                        '1 Corintios 6:19-20','TEMPLO'),
('¿De qué estoy agradecido esta semana?',                                   '1 Tesalonicenses 5:18', NULL);
