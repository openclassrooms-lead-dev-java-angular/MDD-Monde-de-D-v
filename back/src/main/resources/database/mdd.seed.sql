-- seed.sql — données de développement MySQL
-- Génère 20 topics, 20 articles par topic (400 articles) et 5 commentaires par article (2000 commentaires).
--
-- PRÉREQUIS :
--   * La table users contient au moins 5 utilisateurs avec les IDs 1 à 5.
--   * Les colonnes d'audit sont created_at et updated_at, comme supposé ci-dessous.
--   * Les tables et colonnes correspondent aux entités fournies.
--
-- Ce script ajoute des données. Il ne supprime ni ne réinitialise les données existantes.
-- Il utilise des tables temporaires pour relier les données générées à leurs IDs réels,
-- donc il ne dépend pas d'une séquence d'IDs particulière pour topics/articles.

START TRANSACTION;

-- ---------------------------------------------------------
-- 1. TOPICS : 20 sujets
-- ---------------------------------------------------------

INSERT INTO topics (name, description, public_id, created_at, updated_at)
VALUES
('Technologie', 'Innovations, outils numériques et usages technologiques.', UUID(), NOW(), NOW()),
('Lifestyle', 'Habitudes, bien-être quotidien et inspirations.', UUID(), NOW(), NOW()),
('Science', 'Découvertes scientifiques et nouvelles connaissances.', UUID(), NOW(), NOW()),
('Voyage', 'Destinations, cultures et récits de voyage.', UUID(), NOW(), NOW()),
('Cuisine', 'Recettes, gastronomie et produits de saison.', UUID(), NOW(), NOW()),
('Cinéma', 'Films, réalisateurs et culture cinématographique.', UUID(), NOW(), NOW()),
('Littérature', 'Livres, auteurs et plaisirs de la lecture.', UUID(), NOW(), NOW()),
('Musique', 'Genres musicaux, artistes et pratiques musicales.', UUID(), NOW(), NOW()),
('Sport', 'Activité physique, entraînement et esprit d’équipe.', UUID(), NOW(), NOW()),
('Nature', 'Biodiversité, paysages et découverte du vivant.', UUID(), NOW(), NOW()),
('Écologie', 'Transition écologique et gestes responsables.', UUID(), NOW(), NOW()),
('Histoire', 'Événements historiques et mémoire collective.', UUID(), NOW(), NOW()),
('Photographie', 'Techniques photo, composition et regard créatif.', UUID(), NOW(), NOW()),
('Art', 'Arts visuels, créativité et mouvements artistiques.', UUID(), NOW(), NOW()),
('Jeux vidéo', 'Jeux, expériences interactives et culture vidéoludique.', UUID(), NOW(), NOW()),
('Éducation', 'Apprentissage, pédagogie et partage des savoirs.', UUID(), NOW(), NOW()),
('Économie', 'Entreprises, consommation et tendances économiques.', UUID(), NOW(), NOW()),
('Psychologie', 'Comportements, émotions et relations humaines.', UUID(), NOW(), NOW()),
('Jardinage', 'Plantes, potager et aménagement des espaces verts.', UUID(), NOW(), NOW()),
('Bricolage', 'Projets manuels, réparations et astuces pratiques.', UUID(), NOW(), NOW());

-- Conserver les IDs réellement générés, même si la table n'était pas vide.
DROP TEMPORARY TABLE IF EXISTS seed_topic_map;
CREATE TEMPORARY TABLE seed_topic_map (
    topic_name VARCHAR(50) NOT NULL PRIMARY KEY,
    topic_id BIGINT NOT NULL
);

INSERT INTO seed_topic_map (topic_name, topic_id)
SELECT name, id
FROM topics
WHERE name IN (
    'Technologie', 'Lifestyle', 'Science', 'Voyage', 'Cuisine',
    'Cinéma', 'Littérature', 'Musique', 'Sport', 'Nature',
    'Écologie', 'Histoire', 'Photographie', 'Art', 'Jeux vidéo',
    'Éducation', 'Économie', 'Psychologie', 'Jardinage', 'Bricolage'
);

-- ---------------------------------------------------------
-- 2. ARTICLES : 20 par topic = 400
-- ---------------------------------------------------------
-- Les titres sont uniques par topic grâce au numéro d'article.
-- L'auteur est choisi parmi les utilisateurs 1 à 5.
-- Si les IDs 1 à 5 ne sont pas présents, adapte la table de correspondance ci-dessous.

DROP TEMPORARY TABLE IF EXISTS seed_users;
CREATE TEMPORARY TABLE seed_users (
    row_num INT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL
);

INSERT INTO seed_users (row_num, user_id)
SELECT numbered.row_num, numbered.id
FROM (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS row_num
    FROM users
) AS numbered
WHERE numbered.row_num <= 5;

DROP TEMPORARY TABLE IF EXISTS seed_article_map;
CREATE TEMPORARY TABLE seed_article_map (
    topic_id BIGINT NOT NULL,
    article_num INT NOT NULL,
    article_id BIGINT NOT NULL,
    PRIMARY KEY (topic_id, article_num)
);

-- Générer les nombres 1 à 20 via une CTE récursive (MySQL 8+).
INSERT INTO articles (author_id, topic_id, title, content, public_id, created_at, updated_at)
WITH RECURSIVE nums AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 20
)
SELECT
    su.user_id,
    stm.topic_id,
    CONCAT(stm.topic_name, ' — idée et découverte ', LPAD(nums.n, 2, '0')),
    CONCAT(
        'Lorem ipsum dolor sit amet, consectetur adipiscing elit. ',
        'Dans le thème « ', stm.topic_name, ' », ce billet explore une idée, ',
        'partage quelques pistes de réflexion et propose un regard accessible sur le sujet. ',
        'Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. ',
        'Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. ',
        'Article numéro ', nums.n, ' : découvrons ensemble de nouvelles perspectives, ',
        'des exemples concrets et des conseils utiles pour approfondir ce sujet. ',
        'Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.'
    ),
    UUID(),
    NOW(),
    NOW()
FROM seed_topic_map stm
CROSS JOIN nums
JOIN seed_users su ON su.row_num = MOD(nums.n - 1, (SELECT COUNT(*) FROM seed_users)) + 1;

-- Récupérer les articles insérés à partir du motif de titre.
-- Les numéros et les noms de topic rendent chaque titre déterministe.
INSERT INTO seed_article_map (topic_id, article_num, article_id)
SELECT
    t.topic_id,
    CAST(SUBSTRING_INDEX(a.title, ' ', -1) AS UNSIGNED) AS article_num,
    a.id
FROM articles a
JOIN seed_topic_map t ON t.topic_id = a.topic_id
WHERE a.title LIKE '% — idée et découverte %'
  AND a.created_at >= NOW() - INTERVAL 10 MINUTE;

-- ---------------------------------------------------------
-- 3. COMMENTAIRES : 5 par article = 2000
-- ---------------------------------------------------------

INSERT INTO comments (article_id, author_id, content, created_at)
SELECT
    sam.article_id,
    su.user_id,
    CONCAT(
        CASE nums.comment_num
            WHEN 1 THEN 'Merci pour cet article ! '
            WHEN 2 THEN 'Point de vue intéressant. '
            WHEN 3 THEN 'Je n’avais pas envisagé le sujet sous cet angle. '
            WHEN 4 THEN 'Voici une réflexion complémentaire. '
            ELSE 'Très bonne introduction au sujet. '
        END,
        'Lorem ipsum dolor sit amet, consectetur adipiscing elit. ',
        'Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. ',
        'Commentaire ', nums.comment_num, ' pour l’article ', sam.article_num, '.'
    ),
    NOW()
FROM seed_article_map sam
CROSS JOIN (
    SELECT 1 AS comment_num
    UNION ALL SELECT 2
    UNION ALL SELECT 3
    UNION ALL SELECT 4
    UNION ALL SELECT 5
) AS nums
JOIN seed_users su
    ON su.row_num = MOD(sam.article_num + nums.comment_num - 2, (SELECT COUNT(*) FROM seed_users)) + 1;

COMMIT;

