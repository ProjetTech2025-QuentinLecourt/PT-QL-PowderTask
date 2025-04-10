-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : localhost:3306
-- Généré le : jeu. 10 avr. 2025 à 19:15
-- Version du serveur : 11.6.2-MariaDB-log
-- Version de PHP : 8.1.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `powdertask`
--
CREATE DATABASE IF NOT EXISTS `powdertask` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci;
USE `powdertask`;

-- --------------------------------------------------------

--
-- Structure de la table `inventories`
--

CREATE TABLE `inventories` (
  `id` int(11) NOT NULL,
  `powders_id` int(11) NOT NULL,
  `weight_box` float NOT NULL,
  `full_box` int(11) NOT NULL,
  `remain_powder` float DEFAULT NULL,
  `weight_alert` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Déchargement des données de la table `inventories`
--

INSERT INTO `inventories` (`id`, `powders_id`, `weight_box`, `full_box`, `remain_powder`, `weight_alert`) VALUES
(1, 1, 20, 12, 18.75, 5),
(2, 2, 20, 8, 19.5, 5),
(3, 3, 25, 5, 15.25, 5),
(4, 4, 25, 6, 20, 5),
(5, 5, 20, 4, 12.75, 5),
(6, 6, 20, 7, 5.5, 5),
(7, 7, 20, 6, 16.25, 5),
(8, 8, 25, 3, 17.5, 5),
(9, 9, 20, 5, 3.75, 5),
(10, 10, 25, 4, 22.75, 5);

-- --------------------------------------------------------

--
-- Structure de la table `inventory_updates`
--

CREATE TABLE `inventory_updates` (
  `id` int(11) NOT NULL,
  `users_id` int(11) NOT NULL,
  `inventories_id` int(11) NOT NULL,
  `scales_id` int(11) NOT NULL,
  `update_date` datetime NOT NULL,
  `full_box_used` int(11) NOT NULL,
  `remain_powder` float NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Déchargement des données de la table `inventory_updates`
--

INSERT INTO `inventory_updates` (`id`, `users_id`, `inventories_id`, `scales_id`, `update_date`, `full_box_used`, `remain_powder`) VALUES
(1, 2, 1, 3, '2024-03-18 09:15:23', 1, 18.75),
(2, 3, 3, 3, '2024-03-18 10:40:12', 0, 15.25),
(3, 2, 2, 2, '2024-03-21 08:30:45', 1, 19.5),
(4, 2, 5, 3, '2024-03-22 14:20:33', 0, 12.75),
(5, 3, 7, 3, '2024-03-23 11:05:18', 1, 16.25),
(6, 2, 6, 2, '2024-03-24 09:45:56', 0, 5.5),
(7, 3, 4, 3, '2024-03-25 13:10:29', 1, 20),
(8, 2, 9, 3, '2024-03-26 15:30:42', 0, 3.75),
(9, 2, 8, 2, '2024-03-27 10:15:37', 0, 17.5),
(10, 3, 10, 3, '2024-03-28 14:45:22', 1, 22.75);

-- --------------------------------------------------------

--
-- Structure de la table `powders`
--

CREATE TABLE `powders` (
  `id` int(11) NOT NULL,
  `reference` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Déchargement des données de la table `powders`
--

INSERT INTO `powders` (`id`, `reference`) VALUES
(1, 'RAL 9016 - Blanc Trafic'),
(2, 'RAL 9005 - Noir Foncé'),
(3, 'RAL 3020 - Rouge Trafic'),
(4, 'RAL 5010 - Bleu Gentiane'),
(5, 'RAL 6005 - Vert Mousse'),
(6, 'RAL 1021 - Jaune Colza'),
(7, 'RAL 7035 - Gris Clair'),
(8, 'RAL 8017 - Brun Chocolat'),
(9, 'RAL 2004 - Orange Pur'),
(10, 'RAL 5015 - Bleu Ciel');

-- --------------------------------------------------------

--
-- Structure de la table `scales`
--

CREATE TABLE `scales` (
  `id` int(11) NOT NULL,
  `scale_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Déchargement des données de la table `scales`
--

INSERT INTO `scales` (`id`, `scale_name`) VALUES
(1, 'PowderScale - 01'),
(2, 'Balance Secondaire - Atelier B'),
(3, 'Balance Principale - Atelier A');

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `job` enum('OP','CE','P') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`id`, `first_name`, `last_name`, `email`, `password`, `job`) VALUES
(1, 'Quentin', 'Lecourt', 'quentin.lecourt@ps.com', '$2y$10$mwh7Ok9.zhyhAEg8GmcOrOposRvgvY1hVTo6iE.bORQqJcMblDaXy', 'CE'),
(2, 'Quenin', 'Lecourt', 'quentin.p@ps.com', '$2a$12$HxlK.GNc72DP4Q7vKozyY.To5Y3BvWQctyJp.ja5tWgM5eCTf7INK', 'P'),
(3, 'Lucas', 'Petit', 'poudreur.2@ps.com', '$2y$10$FI6/J9JcmTd59/taraGr9OkPdmXe6b7PYyzjVYiXxT/ELGemogELS', 'P'),
(4, 'Julie', 'Leroy', 'op.1@ps.com', '$2y$10$ItAL6CU2BNiIaHMClTkIM.7N0IQ11h/3NzYZ9USzJ7/d.qZS77.Au', 'OP'),
(5, 'Maxime', 'Moreau', 'op.2@ps.com', '$2y$10$ItAL6CU2BNiIaHMClTkIM.7N0IQ11h/3NzYZ9USzJ7/d.qZS77.Au', 'OP');

-- --------------------------------------------------------

--
-- Structure de la table `users_scales`
--

CREATE TABLE `users_scales` (
  `scales_id` int(11) NOT NULL,
  `users_id` int(11) NOT NULL,
  `assignement_date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Déchargement des données de la table `users_scales`
--

INSERT INTO `users_scales` (`scales_id`, `users_id`, `assignement_date`) VALUES
(1, 2, '2025-04-10'),
(2, 3, '2024-03-20');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `inventories`
--
ALTER TABLE `inventories`
  ADD PRIMARY KEY (`id`),
  ADD KEY `powders_id` (`powders_id`);

--
-- Index pour la table `inventory_updates`
--
ALTER TABLE `inventory_updates`
  ADD PRIMARY KEY (`id`),
  ADD KEY `users_id` (`users_id`),
  ADD KEY `inventories_id` (`inventories_id`),
  ADD KEY `inventory_updates_ibfk_3` (`scales_id`);

--
-- Index pour la table `powders`
--
ALTER TABLE `powders`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `scales`
--
ALTER TABLE `scales`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uni_users_email` (`email`);

--
-- Index pour la table `users_scales`
--
ALTER TABLE `users_scales`
  ADD PRIMARY KEY (`scales_id`,`users_id`),
  ADD KEY `users_id` (`users_id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `scales`
--
ALTER TABLE `scales`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT pour la table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `inventories`
--
ALTER TABLE `inventories`
  ADD CONSTRAINT `inventories_ibfk_1` FOREIGN KEY (`powders_id`) REFERENCES `powders` (`id`);

--
-- Contraintes pour la table `inventory_updates`
--
ALTER TABLE `inventory_updates`
  ADD CONSTRAINT `inventory_updates_ibfk_1` FOREIGN KEY (`users_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `inventory_updates_ibfk_2` FOREIGN KEY (`inventories_id`) REFERENCES `inventories` (`id`),
  ADD CONSTRAINT `inventory_updates_ibfk_3` FOREIGN KEY (`scales_id`) REFERENCES `scales` (`id`) ON UPDATE CASCADE;

--
-- Contraintes pour la table `users_scales`
--
ALTER TABLE `users_scales`
  ADD CONSTRAINT `users_scales_ibfk_1` FOREIGN KEY (`scales_id`) REFERENCES `scales` (`id`),
  ADD CONSTRAINT `users_scales_ibfk_2` FOREIGN KEY (`users_id`) REFERENCES `users` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
