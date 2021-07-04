-- 
-- SQL Script to populate words_spa table to use with dibujakka 
--

--
-- words_spa table definition
--
CREATE TABLE IF NOT EXISTS words_spa (
    id integer NOT NULL,
    word varchar NOT NULL,
    difficulty integer DEFAULT 0,
    times_played integer DEFAULT 0,
    times_guessed integer DEFAULT 0
);

--
-- Initial words to play dibujakka
-- Difficulty is kind of arbitrary
--
INSERT INTO words_spa VALUES (1, 'Ángel', 1, 0, 0);
INSERT INTO words_spa VALUES (2, 'Ojo', 1, 0, 0);
INSERT INTO words_spa VALUES (3, 'Pizza', 1, 0, 0);
INSERT INTO words_spa VALUES (4, 'Enojado', 1, 0, 0);
INSERT INTO words_spa VALUES (5, 'Fuegos artificiales', 1, 0, 0);
INSERT INTO words_spa VALUES (6, 'Calabaza', 1, 0, 0);
INSERT INTO words_spa VALUES (7, 'Bebé', 1, 0, 0);
INSERT INTO words_spa VALUES (8, 'Flor', 1, 0, 0);
INSERT INTO words_spa VALUES (9, 'Arco iris', 1, 0, 0);
INSERT INTO words_spa VALUES (10, 'Barba', 1, 0, 0);
INSERT INTO words_spa VALUES (11, 'Platillo volador', 1, 0, 0);
INSERT INTO words_spa VALUES (12, 'Reciclar', 1, 0, 0);
INSERT INTO words_spa VALUES (13, 'Biblia', 1, 0, 0);
INSERT INTO words_spa VALUES (14, 'Jirafa', 1, 0, 0);
INSERT INTO words_spa VALUES (15, 'Castillo de arena', 1, 0, 0);
INSERT INTO words_spa VALUES (16, 'Bikini', 1, 0, 0);
INSERT INTO words_spa VALUES (17, 'Gafas', 1, 0, 0);
INSERT INTO words_spa VALUES (18, 'Copo de nieve', 1, 0, 0);
INSERT INTO words_spa VALUES (19, 'Libro', 1, 0, 0);
INSERT INTO words_spa VALUES (20, 'Tacón', 1, 0, 0);
INSERT INTO words_spa VALUES (21, 'Escalera', 1, 0, 0);
INSERT INTO words_spa VALUES (22, 'Cucurucho de helado', 1, 0, 0);
INSERT INTO words_spa VALUES (23, 'Estrella de mar', 1, 0, 0);
INSERT INTO words_spa VALUES (24, 'Abejorro', 1, 0, 0);
INSERT INTO words_spa VALUES (25, 'Iglú', 1, 0, 0);
INSERT INTO words_spa VALUES (26, 'Fresa', 1, 0, 0);
INSERT INTO words_spa VALUES (27, 'Mariposa', 1, 0, 0);
INSERT INTO words_spa VALUES (28, 'Escarabajo', 1, 0, 0);
INSERT INTO words_spa VALUES (29, 'Sol', 1, 0, 0);
INSERT INTO words_spa VALUES (30, 'Cámara', 1, 0, 0);
INSERT INTO words_spa VALUES (31, 'Lámpara', 1, 0, 0);
INSERT INTO words_spa VALUES (32, 'Neumático', 1, 0, 0);
INSERT INTO words_spa VALUES (33, 'Gato', 1, 0, 0);
INSERT INTO words_spa VALUES (34, 'León', 1, 0, 0);
INSERT INTO words_spa VALUES (35, 'Tostada', 1, 0, 0);
INSERT INTO words_spa VALUES (36, 'Iglesia', 1, 0, 0);
INSERT INTO words_spa VALUES (37, 'Buzón', 1, 0, 0);
INSERT INTO words_spa VALUES (38, 'Cepillo de dientes', 1, 0, 0);
INSERT INTO words_spa VALUES (39, 'Lápiz de color', 1, 0, 0);
INSERT INTO words_spa VALUES (40, 'Noche', 1, 0, 0);
INSERT INTO words_spa VALUES (41, 'Pasta dental', 1, 0, 0);
INSERT INTO words_spa VALUES (42, 'Delfín', 1, 0, 0);
INSERT INTO words_spa VALUES (43, 'Nariz', 1, 0, 0);
INSERT INTO words_spa VALUES (44, 'Camión', 1, 0, 0);
INSERT INTO words_spa VALUES (45, 'Huevo', 1, 0, 0);
INSERT INTO words_spa VALUES (46, 'Juegos Olímpicos', 1, 0, 0);
INSERT INTO words_spa VALUES (47, 'Voleibol', 1, 0, 0);
INSERT INTO words_spa VALUES (48, 'Torre Eiffel', 1, 0, 0);
INSERT INTO words_spa VALUES (49, 'Maní', 1, 0, 0);
INSERT INTO words_spa VALUES (50, 'Beso', 2, 0, 0);
INSERT INTO words_spa VALUES (51, 'Cerebro', 2, 0, 0);
INSERT INTO words_spa VALUES (52, 'Cachorro', 2, 0, 0);
INSERT INTO words_spa VALUES (53, 'Patio de recreo', 2, 0, 0);
INSERT INTO words_spa VALUES (54, 'Britney Spears', 2, 0, 0);
INSERT INTO words_spa VALUES (55, 'Baño de burbujas', 2, 0, 0);
INSERT INTO words_spa VALUES (56, 'Kiwi', 2, 0, 0);
INSERT INTO words_spa VALUES (57, 'Pastel de calabaza', 2, 0, 0);
INSERT INTO words_spa VALUES (58, 'Hebilla', 2, 0, 0);
INSERT INTO words_spa VALUES (59, 'Lápiz labial', 2, 0, 0);
INSERT INTO words_spa VALUES (60, 'Gota de lluvia', 2, 0, 0);
INSERT INTO words_spa VALUES (61, 'Autobús', 2, 0, 0);
INSERT INTO words_spa VALUES (62, 'Langosta', 2, 0, 0);
INSERT INTO words_spa VALUES (63, 'Robot', 2, 0, 0);
INSERT INTO words_spa VALUES (64, 'Accidente automovilistico', 2, 0, 0);
INSERT INTO words_spa VALUES (65, 'Chupete', 2, 0, 0);
INSERT INTO words_spa VALUES (66, 'Castillo de arena', 2, 0, 0);
INSERT INTO words_spa VALUES (67, 'Imán', 2, 0, 0);
INSERT INTO words_spa VALUES (68, 'Zapatilla', 2, 0, 0);
INSERT INTO words_spa VALUES (69, 'Sierra de cadena', 2, 0, 0);
INSERT INTO words_spa VALUES (70, 'Megáfono', 2, 0, 0);
INSERT INTO words_spa VALUES (71, 'Bola de nieve', 2, 0, 0);
INSERT INTO words_spa VALUES (72, 'Tienda de circo', 2, 0, 0);
INSERT INTO words_spa VALUES (73, 'Sirena', 2, 0, 0);
INSERT INTO words_spa VALUES (74, 'Aspersor', 2, 0, 0);
INSERT INTO words_spa VALUES (75, 'Computadora', 2, 0, 0);
INSERT INTO words_spa VALUES (76, 'Minivan', 2, 0, 0);
INSERT INTO words_spa VALUES (77, 'Estatua de la Libertad', 2, 0, 0);
INSERT INTO words_spa VALUES (78, 'Cuna', 2, 0, 0);
INSERT INTO words_spa VALUES (79, 'Monte Everest', 2, 0, 0);
INSERT INTO words_spa VALUES (80, 'Renacuajo', 2, 0, 0);
INSERT INTO words_spa VALUES (81, 'Dragón', 2, 0, 0);
INSERT INTO words_spa VALUES (82, 'Música', 2, 0, 0);
INSERT INTO words_spa VALUES (83, 'Campamento', 2, 0, 0);
INSERT INTO words_spa VALUES (84, 'Pesa', 2, 0, 0);
INSERT INTO words_spa VALUES (85, 'Polo Norte', 2, 0, 0);
INSERT INTO words_spa VALUES (86, 'Telescopio', 2, 0, 0);
INSERT INTO words_spa VALUES (87, 'Anguila', 2, 0, 0);
INSERT INTO words_spa VALUES (88, 'Enfermera', 2, 0, 0);
INSERT INTO words_spa VALUES (89, 'Tren', 2, 0, 0);
INSERT INTO words_spa VALUES (90, 'Rueda de la fortuna', 2, 0, 0);
INSERT INTO words_spa VALUES (91, 'Búho', 2, 0, 0);
INSERT INTO words_spa VALUES (92, 'Triciclo', 2, 0, 0);
INSERT INTO words_spa VALUES (93, 'Bandera', 2, 0, 0);
INSERT INTO words_spa VALUES (94, 'Chupete', 2, 0, 0);
INSERT INTO words_spa VALUES (95, 'Tutú', 2, 0, 0);
INSERT INTO words_spa VALUES (96, 'Correo no deseado', 2, 0, 0);
INSERT INTO words_spa VALUES (97, 'Piano', 2, 0, 0);
INSERT INTO words_spa VALUES (98, 'Ático', 3, 0, 0);
INSERT INTO words_spa VALUES (99, 'Pegamento', 3, 0, 0);
INSERT INTO words_spa VALUES (100, 'Reloj de bolsillo', 3, 0, 0);
INSERT INTO words_spa VALUES (101, 'Asiento trasero', 3, 0, 0);
INSERT INTO words_spa VALUES (102, 'Silla alta', 3, 0, 0);
INSERT INTO words_spa VALUES (103, 'Banda de rock', 3, 0, 0);
INSERT INTO words_spa VALUES (104, 'México', 3, 0, 0);
INSERT INTO words_spa VALUES (105, 'Cumpleaños', 3, 0, 0);
INSERT INTO words_spa VALUES (106, 'Hockey', 3, 0, 0);
INSERT INTO words_spa VALUES (107, 'Piegrande', 3, 0, 0);
INSERT INTO words_spa VALUES (108, 'Calabozo', 3, 0, 0);
INSERT INTO words_spa VALUES (109, 'Hotel', 3, 0, 0);
INSERT INTO words_spa VALUES (110, 'Huevos revueltos', 3, 0, 0);
INSERT INTO words_spa VALUES (111, 'Tormenta de nieve', 3, 0, 0);
INSERT INTO words_spa VALUES (112, 'Cuerda de saltar', 3, 0, 0);
INSERT INTO words_spa VALUES (113, 'Cinturón de seguridad', 3, 0, 0);
INSERT INTO words_spa VALUES (114, 'Burrito', 3, 0, 0);
INSERT INTO words_spa VALUES (115, 'Koala', 3, 0, 0);
INSERT INTO words_spa VALUES (116, 'Ignorar', 3, 0, 0);
INSERT INTO words_spa VALUES (117, 'Capitán', 3, 0, 0);
INSERT INTO words_spa VALUES (118, 'Duende', 3, 0, 0);
INSERT INTO words_spa VALUES (119, 'Eclipse solar', 3, 0, 0);
INSERT INTO words_spa VALUES (120, 'Candelabro', 3, 0, 0);
INSERT INTO words_spa VALUES (121, 'Rápido', 3, 0, 0);
INSERT INTO words_spa VALUES (122, 'Espacio', 3, 0, 0);
INSERT INTO words_spa VALUES (123, 'Cuna', 3, 0, 0);
INSERT INTO words_spa VALUES (124, 'Máscara', 3, 0, 0);
INSERT INTO words_spa VALUES (125, 'Estetoscopio', 3, 0, 0);
INSERT INTO words_spa VALUES (126, 'Crucero', 3, 0, 0);
INSERT INTO words_spa VALUES (127, 'Mecánico', 3, 0, 0);
INSERT INTO words_spa VALUES (128, 'Cigüeña', 3, 0, 0);
INSERT INTO words_spa VALUES (129, 'Baile', 3, 0, 0);
INSERT INTO words_spa VALUES (130, 'Mamá', 3, 0, 0);
INSERT INTO words_spa VALUES (131, 'Bronceado', 3, 0, 0);
INSERT INTO words_spa VALUES (132, 'Desodorante', 3, 0, 0);
INSERT INTO words_spa VALUES (133, 'Señor Cara de Papa', 3, 0, 0);
INSERT INTO words_spa VALUES (134, 'Hilo', 3, 0, 0);
INSERT INTO words_spa VALUES (135, 'Facebook', 3, 0, 0);
INSERT INTO words_spa VALUES (136, 'Saturno', 3, 0, 0);
INSERT INTO words_spa VALUES (137, 'Turista', 3, 0, 0);
INSERT INTO words_spa VALUES (138, 'Plano', 3, 0, 0);
INSERT INTO words_spa VALUES (139, 'Plato de papel', 3, 0, 0);
INSERT INTO words_spa VALUES (140, 'Estados Unidos', 3, 0, 0);
INSERT INTO words_spa VALUES (141, 'Marco', 3, 0, 0);
INSERT INTO words_spa VALUES (142, 'Foto', 3, 0, 0);
INSERT INTO words_spa VALUES (143, 'WIFI', 3, 0, 0);
INSERT INTO words_spa VALUES (144, 'Luna llena', 3, 0, 0);
INSERT INTO words_spa VALUES (145, 'Monja', 3, 0, 0);
INSERT INTO words_spa VALUES (146, 'Zombi', 3, 0, 0);
INSERT INTO words_spa VALUES (147, 'Juego', 3, 0, 0);
INSERT INTO words_spa VALUES (148, 'Pirata', 3, 0, 0);

COMMIT;

ANALYZE;
