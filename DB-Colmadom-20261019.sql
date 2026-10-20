CREATE DATABASE  IF NOT EXISTS `db_colmadom` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `db_colmadom`;
-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: db_colmadom
-- ------------------------------------------------------
-- Server version	8.4.4

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ajuste_inventario`
--

DROP TABLE IF EXISTS `ajuste_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ajuste_inventario` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `fecha_registro` datetime NOT NULL,
  `usuario` int NOT NULL,
  `tipo_ajuste` int NOT NULL,
  `observacion` text NOT NULL,
  `anulado` tinyint(1) NOT NULL,
  `nombre_usuario` varchar(80) DEFAULT NULL,
  `fecha_anulado` date DEFAULT NULL,
  `almacen` int DEFAULT NULL,
  `nombre_almacen` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_ajuste_inv_usuario` (`usuario`),
  KEY `fk_ajuste_inv_tipo` (`tipo_ajuste`),
  KEY `fk_ajuste_inv_alm_idx` (`almacen`),
  CONSTRAINT `fk_ajuste_inv_alm` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_ajuste_inv_tipo` FOREIGN KEY (`tipo_ajuste`) REFERENCES `tipo_ajuste` (`codigo`),
  CONSTRAINT `fk_ajuste_inv_usuario` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ajuste_inventario`
--

LOCK TABLES `ajuste_inventario` WRITE;
/*!40000 ALTER TABLE `ajuste_inventario` DISABLE KEYS */;
INSERT INTO `ajuste_inventario` VALUES (86,'2026-10-14','2026-10-14 11:35:15',1,1,'incremento ',0,'ADMIN',NULL,1,'ALMCEN  PRINCIPAL'),(87,'2026-10-14','2026-10-14 11:36:03',1,2,'ajuste',0,'ADMIN',NULL,1,'ALMCEN  PRINCIPAL'),(88,'2026-10-14','2026-10-14 13:36:10',1,1,'aumento 4',0,'ADMIN',NULL,2,'DISPONIBLE PARA LA VENTA'),(89,'2026-10-15','2026-10-15 15:11:32',1,1,'a cero',0,'ADMIN',NULL,2,'DISPONIBLE PARA LA VENTA'),(90,'2026-10-16','2026-10-16 11:42:08',1,2,'DISMINUCION DE 6 UN',0,'ADMIN',NULL,2,'DISPONIBLE PARA LA VENTA'),(91,'2026-10-19','2026-10-19 13:27:28',1,2,'POR MERMA',0,'ADMIN',NULL,2,'DISPONIBLE PARA LA VENTA');
/*!40000 ALTER TABLE `ajuste_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `almacen`
--

DROP TABLE IF EXISTS `almacen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `almacen` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) DEFAULT NULL,
  `ubicacion` varchar(120) DEFAULT NULL,
  `fecha_creacion` timestamp NOT NULL,
  `creado_por` varchar(50) NOT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `almacen`
--

LOCK TABLES `almacen` WRITE;
/*!40000 ALTER TABLE `almacen` DISABLE KEYS */;
INSERT INTO `almacen` VALUES (1,'ALMCEN  PRINCIPAL','LOCAL','2026-09-15 04:00:00','ADMIN'),(2,'DISPONIBLE PARA LA VENTA','LOCAL','2026-09-25 04:00:00','ADMIN');
/*!40000 ALTER TABLE `almacen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `articulo`
--

DROP TABLE IF EXISTS `articulo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `articulo` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `numero` int NOT NULL,
  `descripcion` text,
  `codigo_de_barra` varchar(80) DEFAULT NULL,
  `categoria` int DEFAULT NULL,
  `sub_categoria` int DEFAULT NULL,
  `existencia` decimal(12,2) DEFAULT NULL,
  `maximo` decimal(12,2) DEFAULT NULL,
  `minimo` decimal(12,2) DEFAULT NULL,
  `precio_compra` decimal(12,2) DEFAULT NULL,
  `precio_venta` decimal(12,2) DEFAULT NULL,
  `precio_venta_con_itbis` double DEFAULT NULL,
  `precio_venta_anterior` double DEFAULT NULL,
  `exento_itbis` bit(1) NOT NULL DEFAULT b'0',
  `inventariable` bit(1) NOT NULL,
  `permitir_venta_sin_existencia` bit(1) NOT NULL DEFAULT b'0',
  `tipo_articulo` int DEFAULT NULL,
  `margen_beneficio` double DEFAULT NULL,
  `porciento_utilidad` double DEFAULT NULL,
  `modelo` varchar(25) DEFAULT NULL,
  `marca` varchar(80) DEFAULT NULL,
  `linea_articulo` int DEFAULT NULL,
  `nombre_linea` varchar(80) DEFAULT NULL,
  `para_venta` bit(1) NOT NULL,
  `para_consumo` bit(1) NOT NULL,
  `ruta_img` varchar(80) DEFAULT NULL,
  `compuesto` bit(1) NOT NULL DEFAULT b'0',
  `secuencia_documento` int DEFAULT NULL,
  `itbis_gravado` double DEFAULT NULL,
  `venta_agranel` bit(1) NOT NULL DEFAULT b'0',
  `unidad_de_venta` int DEFAULT NULL,
  `creado_por` varchar(50) DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `perecedero` bit(1) NOT NULL DEFAULT b'0',
  `fecha_vencimiento` date DEFAULT NULL,
  `habilitado` bit(1) NOT NULL,
  `unidad_base` int DEFAULT NULL,
  `nombre_unidad_base` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `categoria` (`categoria`),
  KEY `sub_categoria` (`sub_categoria`),
  KEY `tipo_articulo` (`tipo_articulo`),
  KEY `fk_articulo_secuecia_doc` (`secuencia_documento`),
  KEY `fk_articulo_unidad_de_venta` (`unidad_de_venta`),
  KEY `fk_articulo_unidad_idx` (`unidad_base`),
  CONSTRAINT `fk_articulo_unidad` FOREIGN KEY (`unidad_base`) REFERENCES `unidad` (`codigo`),
  CONSTRAINT `fk_articulo_unidad_de_venta` FOREIGN KEY (`unidad_de_venta`) REFERENCES `unidad_de_venta` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articulo`
--

LOCK TABLES `articulo` WRITE;
/*!40000 ALTER TABLE `articulo` DISABLE KEYS */;
INSERT INTO `articulo` VALUES (1,0,'SALSA LINDA DE  1 KG',NULL,NULL,NULL,28.00,NULL,NULL,50.00,60.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '',1,'UNIDAD'),(2,0,'LECHE RICA LISTA MIL ',NULL,NULL,NULL,84.00,NULL,NULL,80.00,90.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '',1,'UNIDAD'),(3,0,'SOPITA DOÃ‘A GALLINA',NULL,NULL,NULL,3.00,NULL,NULL,10.00,15.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '',6,'SOBRE'),(4,0,'SAZON RANCHERO',NULL,NULL,NULL,26.00,NULL,NULL,10.00,15.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '',1,'UNIDAD'),(5,0,'LECHE NIDO DE UN LT',NULL,NULL,NULL,39.00,NULL,NULL,35.00,45.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '',1,'UNIDAD'),(6,0,'ARROZ PINCO DE 1LB',NULL,NULL,NULL,1528.00,NULL,NULL,25.00,30.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '',1,'UNIDAD'),(7,0,'PLATANO VERDE',NULL,NULL,NULL,37.00,NULL,NULL,12.00,20.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '',1,'UNIDAD'),(8,0,'QUESO CHEDA  LB',NULL,NULL,NULL,0.00,NULL,NULL,150.00,180.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',2,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '',NULL,NULL),(9,0,'CERVESA PRESIDENTE PQ',NULL,NULL,NULL,0.00,NULL,NULL,55.00,75.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '',NULL,NULL),(12,0,'BERENJENA MORADA',NULL,NULL,NULL,0.00,NULL,NULL,12.00,15000.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '\0',NULL,NULL),(27,0,'FOSFORO',NULL,NULL,NULL,0.00,NULL,NULL,10.00,15.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',NULL,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '\0',NULL,NULL),(28,0,'AVNA FRESCA',NULL,NULL,NULL,0.00,NULL,NULL,40.00,50.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '\0',NULL,NULL),(31,0,'QUESO GEO',NULL,NULL,NULL,0.00,NULL,NULL,10.00,15.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',NULL,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '\0',NULL,NULL),(32,0,'SALAMI ESTELAR',NULL,NULL,NULL,4.00,NULL,NULL,40.00,50.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '\0',1,'UNIDAD'),(33,0,'ACEITE CRISOL ',NULL,NULL,NULL,238.00,NULL,NULL,60.00,80.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',NULL,_binary '\0',1,'UNIDAD'),(34,0,'ARROZ  LA GARZA DE  1LBS',NULL,NULL,NULL,39.00,NULL,NULL,25.00,30.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,NULL,_binary '\0',1,NULL,NULL,_binary '\0',NULL,_binary '\0',1,'UNIDAD');
/*!40000 ALTER TABLE `articulo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `articulo_almacen`
--

DROP TABLE IF EXISTS `articulo_almacen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `articulo_almacen` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `articulo` int NOT NULL,
  `descripcion_articulo` varchar(100) DEFAULT NULL,
  `almacen` int NOT NULL,
  `nombre_almacen` varchar(50) DEFAULT NULL,
  `existencia` decimal(12,2) NOT NULL,
  `minimo` decimal(12,2) NOT NULL,
  `maximo` decimal(12,2) NOT NULL,
  `unidad` int DEFAULT NULL,
  `nombre_unidad` varchar(20) DEFAULT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT NULL,
  `creado_por` varchar(50) DEFAULT NULL,
  `actualizado_por` varchar(50) DEFAULT NULL,
  `ubicacion_pasillo` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_existencia_articulo` (`articulo`) USING BTREE,
  KEY `fk_existencia_almacen` (`almacen`) USING BTREE,
  KEY `fk_existencia_art_unidad` (`unidad`),
  CONSTRAINT `articulo_almacen_art` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `articulo_almacen_ibfk_1` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `articulo_almacen_ibfk_4` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articulo_almacen`
--

LOCK TABLES `articulo_almacen` WRITE;
/*!40000 ALTER TABLE `articulo_almacen` DISABLE KEYS */;
INSERT INTO `articulo_almacen` VALUES (1,3,'SOPITA DOÃ‘A GALLINA',1,'ALMCEN  PRINCIPAL',2.00,5.00,999999.00,6,'SOBRE','2026-10-15 18:57:53','2026-10-15 18:57:53','admin',NULL,'A012'),(2,3,'SOPITA DOÃ‘A GALLINA',2,'DISPONIBLE PARA LA VENTA',6.00,5.00,5.00,6,'SOBRE','2026-10-15 18:57:53','2026-10-15 18:57:53','admin',NULL,'A012'),(11,27,'FOSFORO',1,'ALMCEN  PRINCIPAL',0.00,10.00,999999.00,1,'UNIDAD','2026-10-06 17:21:24','2026-10-06 17:21:24','admin',NULL,'A012'),(12,28,'AVNA FRESCA',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,1,'UNIDAD','2026-10-06 17:31:10','2026-10-06 17:31:10','admin',NULL,'A013'),(13,31,'QUESO GEO',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,4,'LIBRA','2026-10-06 17:56:50','2026-10-06 17:56:50','admin',NULL,'A012'),(14,31,'QUESO GEO',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,4,'LIBRA','2026-10-06 17:56:50','2026-10-06 17:56:50','admin',NULL,'A012'),(15,32,'SALAMI ESTELAR',1,'ALMCEN  PRINCIPAL',0.00,5.00,10.00,1,'UNIDAD','2026-10-09 15:06:21','2026-10-09 15:06:21','admin',NULL,'A012'),(16,32,'SALAMI ESTELAR',2,'DISPONIBLE PARA LA VENTA',0.00,1.00,1.00,1,'UNIDAD','2026-10-09 15:06:21','2026-10-09 15:06:21','admin',NULL,'A012'),(17,33,'ACEITE CRISOL ',1,'ALMCEN  PRINCIPAL',263.00,0.00,999999.00,1,'UNIDAD','2026-10-15 17:50:12','2026-10-15 17:50:12','admin',NULL,'A012'),(18,33,'ACEITE CRISOL ',2,'DISPONIBLE PARA LA VENTA',-9.00,0.00,999999.00,1,'UNIDAD','2026-10-15 17:50:12','2026-10-15 17:50:12','admin',NULL,'A012'),(19,1,'SALSA LINDA DE  1 KG',1,'ALMCEN  PRINCIPAL',23.00,0.00,999999.00,1,'UNIDAD','2026-10-08 15:11:03','2026-10-08 15:11:03','admin',NULL,'A012'),(20,1,'SALSA LINDA DE  1 KG',2,'DISPONIBLE PARA LA VENTA',3.00,0.00,999999.00,1,'UNIDAD','2026-10-08 15:11:03','2026-10-08 15:11:03','admin',NULL,'A012'),(21,2,'LECHE RICA LISTA MIL ',1,'ALMCEN  PRINCIPAL',68.00,0.00,999999.00,1,'UNIDAD','2026-10-17 15:30:34','2026-10-17 15:30:34','admin',NULL,'A012'),(22,2,'LECHE RICA LISTA MIL ',2,'DISPONIBLE PARA LA VENTA',16.00,0.00,999999.00,1,'UNIDAD','2026-10-17 15:30:34','2026-10-17 15:30:34','admin',NULL,'A012'),(23,34,'ARROZ  LA GARZA DE  1LBS',1,'ALMCEN  PRINCIPAL',7.00,0.00,999999.00,1,'UNIDAD','2026-10-14 15:40:26','2026-10-14 15:40:26','admin',NULL,'A012'),(24,34,'ARROZ  LA GARZA DE  1LBS',2,'DISPONIBLE PARA LA VENTA',32.00,0.00,999999.00,1,'UNIDAD','2026-10-14 15:40:26','2026-10-14 15:40:26','admin',NULL,'A012'),(25,4,'SAZON RANCHERO',1,'ALMCEN  PRINCIPAL',9.00,0.00,999999.00,1,'UNIDAD','2026-10-12 20:21:42','2026-10-12 20:21:42','admin',NULL,'A012'),(26,4,'SAZON RANCHERO',2,'DISPONIBLE PARA LA VENTA',17.00,0.00,999999.00,1,'UNIDAD','2026-10-12 20:21:42','2026-10-12 20:21:42','admin',NULL,'A012'),(27,6,'ARROZ PINCO DE 1LB',1,'ALMCEN  PRINCIPAL',1516.00,0.00,999999.00,1,'UNIDAD','2026-10-16 20:45:29','2026-10-16 20:45:29','admin',NULL,'A012'),(28,6,'ARROZ PINCO DE 1LB',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,1,'UNIDAD','2026-10-16 20:45:29','2026-10-16 20:45:29','admin',NULL,'A012'),(29,5,'LECHE NIDO DE UN LT',1,'ALMCEN  PRINCIPAL',30.00,0.00,999999.00,1,'UNIDAD','2026-10-15 15:32:43','2026-10-15 15:32:43','admin',NULL,'A012'),(30,5,'LECHE NIDO DE UN LT',2,'DISPONIBLE PARA LA VENTA',9.00,0.00,999999.00,1,'UNIDAD','2026-10-15 15:32:43','2026-10-15 15:32:43','admin',NULL,'A012'),(33,7,'PLATANO VERDE',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,1,'UNIDAD','2026-10-19 17:22:37','2026-10-19 17:22:37','admin',NULL,'A012'),(34,7,'PLATANO VERDE',2,'DISPONIBLE PARA LA VENTA',37.00,0.00,999999.00,1,'UNIDAD','2026-10-19 17:22:37','2026-10-19 17:22:37','admin',NULL,'A012');
/*!40000 ALTER TABLE `articulo_almacen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `articulo_empaque`
--

DROP TABLE IF EXISTS `articulo_empaque`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `articulo_empaque` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `articulo` int NOT NULL,
  `nombre_articulo` varchar(60) DEFAULT NULL,
  `unidad_empaque` int NOT NULL,
  `nombre_empaque` varchar(25) DEFAULT NULL,
  `descripcion_empaque` varchar(60) DEFAULT NULL,
  `factor_conversion` decimal(12,2) NOT NULL DEFAULT '1.00',
  `codigo_barra` varchar(50) DEFAULT NULL,
  `precio_compra` decimal(12,2) DEFAULT NULL,
  `precio_venta` decimal(12,2) DEFAULT NULL,
  `se_compra_en` tinyint(1) DEFAULT '0',
  `se_vende_en` tinyint(1) DEFAULT '0',
  `es_empaque_base` tinyint(1) DEFAULT '0',
  `estado` tinyint(1) DEFAULT '1',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `creado_por` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `idx_art_emp_articulo` (`articulo`),
  KEY `idx_art_emp_barcode` (`codigo_barra`),
  KEY `fk_articulo_empaque_unidad_idx` (`unidad_empaque`),
  CONSTRAINT `fk_articulo_empaque_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`) ON DELETE CASCADE,
  CONSTRAINT `fk_articulo_empaque_unidad` FOREIGN KEY (`unidad_empaque`) REFERENCES `unidad` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articulo_empaque`
--

LOCK TABLES `articulo_empaque` WRITE;
/*!40000 ALTER TABLE `articulo_empaque` DISABLE KEYS */;
INSERT INTO `articulo_empaque` VALUES (51,33,'ACEITE CRISOL ',1,'UNIDAD','UNIDAD',1.00,'7100',60.00,80.00,0,1,1,1,NULL,'admin'),(52,33,'ACEITE CRISOL',3,'CAJA','CAJA',10.00,'7001',600.00,800.00,1,1,0,1,NULL,'admin'),(53,1,'SALSA LINDA DE  1 KG',1,'UNIDAD','UNIDAD',1.00,NULL,50.00,60.00,1,1,1,1,NULL,'admin'),(54,32,'SALAMI ESTELAR',1,'UNIDAD','UNIDAD',1.00,'2001',250.00,300.00,1,1,1,1,NULL,'admin'),(55,32,'SALAMI ESTELAR',2,'PAQUETE','PAQUETE',5.00,'20002',1250.00,1500.00,1,0,0,1,NULL,'admin'),(62,2,'LECHE RICA LISTA MIL ',1,'UNIDAD','CAJITA DE 1L',1.00,'20009',50.00,70.00,0,1,1,1,NULL,'admin'),(63,2,'LECHE RICA LISTA MIL',3,'CAJA','CAJA DE 20',10.00,'200010',500.00,700.00,1,1,0,1,NULL,'admin'),(64,2,'LECHE RICA LISTA MIL ',2,'PAQUETE','PAQUETE DE  5',5.00,'200011',250.00,300.00,1,1,0,1,NULL,'admin'),(65,32,'SALAMI ESTELAR',7,'FALDO','FALDO',4.00,'200012',1600.00,1800.00,1,1,0,1,NULL,'admin'),(66,34,'ARROZ  LA GARZA DE  1LBS',1,'UNIDAD','UNIDAD',1.00,'1001',25.00,30.00,1,1,1,1,NULL,'admin'),(67,34,'ARROZ  LA GARZA DE  1LBS',7,'FALDO','FALDO',10.00,'1002',250.00,300.00,1,0,0,1,NULL,'admin'),(68,4,'SAZON RANCHERO',1,'UNIDAD','UNIDAD',1.00,NULL,10.00,15.00,1,1,1,1,NULL,'admin'),(69,6,'ARROZ PINCO DE 1LB',1,'UNIDAD','UNIDAD',1.00,'',25.00,30.00,0,1,1,1,NULL,'admin'),(70,6,'ARROZ PINCO DE 1LB',5,'SACO','SACO',300.00,'30012',7500.00,9000.00,1,0,0,1,NULL,'admin'),(71,5,'LECHE NIDO DE UN LT',1,'UNIDAD','UNIDAD',1.00,'1321',35.00,45.00,0,1,1,1,NULL,'admin'),(72,5,'LECHE NIDO DE UN LT',3,'CAJA','CAJA',15.00,'1231',525.00,675.00,1,0,0,1,NULL,'admin'),(73,3,'SOPITA DOÃ‘A GALLINA',6,'SOBRE','SOBRE',1.00,'3331',10.00,15.00,0,1,1,1,NULL,'admin'),(74,3,'SOPITA DOÃ‘A GALLINA',3,'CAJA','CAJA',48.00,'33321',480.00,720.00,1,1,0,1,NULL,'admin'),(75,7,'PLATANO VERDE',1,'UNIDAD','UNIDAD',1.00,'1',12.00,20.00,1,1,1,NULL,NULL,'admin');
/*!40000 ALTER TABLE `articulo_empaque` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `caja_chica`
--

DROP TABLE IF EXISTS `caja_chica`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `caja_chica` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `punto_de_venta` varchar(45) NOT NULL,
  `fecha_apertura` datetime NOT NULL,
  `monto_apertura` double NOT NULL,
  `fecha_cierre` datetime DEFAULT NULL,
  `monto_cierre` double DEFAULT NULL,
  `monto_cierre_esperado` double DEFAULT NULL,
  `monto_cierre_real` double DEFAULT NULL,
  `usuario_apertura` varchar(200) NOT NULL,
  `usuario_cierre` varchar(200) DEFAULT NULL,
  `abierta` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `caja_chica`
--

LOCK TABLES `caja_chica` WRITE;
/*!40000 ALTER TABLE `caja_chica` DISABLE KEYS */;
/*!40000 ALTER TABLE `caja_chica` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `caja_turno`
--

DROP TABLE IF EXISTS `caja_turno`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `caja_turno` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_apertura` datetime NOT NULL,
  `fecha_cierre` datetime DEFAULT NULL,
  `monto_apertura` decimal(12,2) NOT NULL,
  `monto_cierre_real` decimal(12,2) DEFAULT NULL,
  `ventas_efectivo_esperadas` decimal(12,2) DEFAULT NULL,
  `ventas_tarjeta` decimal(12,2) DEFAULT NULL,
  `ventas_fiado` decimal(12,2) DEFAULT NULL,
  `diferencia` decimal(12,2) DEFAULT NULL,
  `cajero_apertura` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cajero_cierre` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `observaciones` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `caja_turno`
--

LOCK TABLES `caja_turno` WRITE;
/*!40000 ALTER TABLE `caja_turno` DISABLE KEYS */;
INSERT INTO `caja_turno` VALUES (35,'2026-10-08 17:42:07','2026-10-14 11:00:57',2000.00,2000.00,NULL,NULL,NULL,0.00,'Admin','Admin','Cerrado','Apertura con monto mayor'),(36,'2026-10-14 11:32:21',NULL,1500.00,NULL,NULL,NULL,NULL,NULL,'Admin',NULL,'Abierta','Apertura con monto mayor');
/*!40000 ALTER TABLE `caja_turno` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) NOT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creada_por` varchar(50) DEFAULT NULL,
  `habilitada` bit(1) NOT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categoria`
--

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(150) DEFAULT NULL,
  `cedula` varchar(20) DEFAULT NULL,
  `celular` varchar(15) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `lugar_de_trabajo` varchar(150) DEFAULT NULL,
  `actualizado_por` varchar(90) DEFAULT NULL,
  `referencia` varchar(45) DEFAULT NULL,
  `fecha_creacion` datetime DEFAULT NULL,
  `creado_por` varchar(90) DEFAULT NULL,
  `fecha_actualizacion` datetime DEFAULT NULL,
  `habilitado` bit(1) DEFAULT NULL,
  `limite_credito` double DEFAULT NULL,
  `apodo` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cliente`
--

LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` VALUES (13,'PUBLICO GENERAL','00','00','EMPRESA','EMPRESA',NULL,'EMPRESA','2025-09-07 10:35:40',NULL,'2025-09-07 10:35:40',_binary '',NULL,NULL),(14,'MAXIMILIANO ALMONTE RODRIGUEZ','034-0044420-8','809-280-7944','Las colinas santiago  c/29 #5','cementosc ibao',NULL,'nino','2026-08-25 10:02:58',NULL,'2026-08-25 10:02:58',_binary '\0',NULL,NULL),(15,'JOSE ALMANZAR','0000','809-280-4521','25-10 LAS COLINAS SANTIAGO','',NULL,'','2026-09-11 11:03:35',NULL,'2026-09-11 11:03:35',_binary '\0',NULL,NULL);
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `configuracion`
--

DROP TABLE IF EXISTS `configuracion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `configuracion` (
  `id` int NOT NULL DEFAULT '1',
  `nombre_empresa` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT 'Mi Empresa',
  `rnc` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `almacen_entrada_id` int DEFAULT NULL,
  `almacen_venta_id` int DEFAULT NULL,
  `permitir_cambio_almacen` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_config_almacen_entrada` (`almacen_entrada_id`),
  KEY `fk_config_almacen_venta` (`almacen_venta_id`),
  CONSTRAINT `fk_config_almacen_entrada` FOREIGN KEY (`almacen_entrada_id`) REFERENCES `almacen` (`codigo`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_config_almacen_venta` FOREIGN KEY (`almacen_venta_id`) REFERENCES `almacen` (`codigo`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `configuracion`
--

LOCK TABLES `configuracion` WRITE;
/*!40000 ALTER TABLE `configuracion` DISABLE KEYS */;
INSERT INTO `configuracion` VALUES (1,'Mi Empresa S.R.L.',NULL,NULL,NULL,0);
/*!40000 ALTER TABLE `configuracion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `delivery`
--

DROP TABLE IF EXISTS `delivery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `delivery` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(80) NOT NULL,
  `apellido` varchar(50) NOT NULL,
  `celular` varchar(15) NOT NULL,
  `direccion` varchar(80) NOT NULL,
  `fecha` date NOT NULL,
  `estado` varchar(10) NOT NULL,
  `foto` longblob,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery`
--

LOCK TABLES `delivery` WRITE;
/*!40000 ALTER TABLE `delivery` DISABLE KEYS */;
INSERT INTO `delivery` VALUES (1,'RAMON ALMONTE','ALMONTE','00000','LAS COLINAS','2026-08-25','DISPONIBLE',NULL),(2,'JOSE CARRAZCO','CARRZZCO','000','GURAVITO','2026-08-26','DISPONIBLE',NULL);
/*!40000 ALTER TABLE `delivery` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `desglose_caja`
--

DROP TABLE IF EXISTS `desglose_caja`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `desglose_caja` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `caja_turno` bigint NOT NULL,
  `denominacion` decimal(10,2) NOT NULL,
  `cantidad` int NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_desglose_caja_turno` (`caja_turno`),
  CONSTRAINT `fk_desglose_caja_turno` FOREIGN KEY (`caja_turno`) REFERENCES `caja_turno` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `desglose_caja`
--

LOCK TABLES `desglose_caja` WRITE;
/*!40000 ALTER TABLE `desglose_caja` DISABLE KEYS */;
/*!40000 ALTER TABLE `desglose_caja` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_ajuste_inventario`
--

DROP TABLE IF EXISTS `detalle_ajuste_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_ajuste_inventario` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `ajuste_inventario` int NOT NULL,
  `articulo` int NOT NULL,
  `descripcion_articulo` varchar(120) NOT NULL,
  `existencia` decimal(12,2) NOT NULL,
  `cantidad` decimal(12,2) NOT NULL,
  `nueva_existencia` decimal(12,2) NOT NULL,
  `unidad` int NOT NULL,
  `nombre_unidad` varchar(20) DEFAULT NULL,
  `almacen` int DEFAULT NULL,
  `nombre_almacen` varchar(80) DEFAULT NULL,
  `articulo_empaque` int DEFAULT NULL,
  `factor_conversion` decimal(12,2) DEFAULT NULL,
  `cantidad_fisica_base` decimal(12,2) DEFAULT NULL,
  `precio_compra` decimal(12,2) DEFAULT NULL,
  `costo_unitario` decimal(12,2) DEFAULT NULL,
  `sub_total` decimal(12,2) DEFAULT NULL,
  `itbis` decimal(12,2) DEFAULT NULL,
  `total` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_det_ajusute_articulo` (`articulo`),
  KEY `fk_det_ajuste_inventario` (`ajuste_inventario`),
  KEY `fk_det_ajuste_unidad` (`unidad`),
  KEY `fk_det_ajusute_almacen` (`almacen`),
  KEY `fk_det_ajusute_art_empq_idx` (`articulo_empaque`),
  CONSTRAINT `fk_det_ajuste_inventario` FOREIGN KEY (`ajuste_inventario`) REFERENCES `ajuste_inventario` (`codigo`),
  CONSTRAINT `fk_det_ajuste_unidad` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`),
  CONSTRAINT `fk_det_ajusute_almacen` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_det_ajusute_art_empq` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_det_ajusute_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_ajuste_inventario`
--

LOCK TABLES `detalle_ajuste_inventario` WRITE;
/*!40000 ALTER TABLE `detalle_ajuste_inventario` DISABLE KEYS */;
INSERT INTO `detalle_ajuste_inventario` VALUES (81,86,34,'ARROZ  LA GARZA DE  1LBS',7.00,2.00,27.00,1,'FALDO',1,'ALMCEN  PRINCIPAL',67,10.00,20.00,250.00,250.00,250.00,0.00,250.00),(82,87,34,'ARROZ  LA GARZA DE  1LBS',27.00,1.00,17.00,1,'FALDO',1,'ALMCEN  PRINCIPAL',67,10.00,10.00,250.00,250.00,250.00,0.00,250.00),(83,88,33,'ACEITE CRISOL UN LITRO',-4.00,4.00,0.00,1,'UNIDAD',2,'DISPONIBLE PARA LA VENTA',51,1.00,4.00,60.00,60.00,60.00,10.80,70.80),(84,89,3,'SOPITA DOÃ‘A GALLINA',-43.00,100.00,57.00,6,'SOBRE',2,'DISPONIBLE PARA LA VENTA',73,1.00,100.00,10.00,10.00,10.00,1.80,11.80),(85,90,2,'LECHE RICA LISTA MIL ',26.00,6.00,20.00,1,'UNIDAD',2,'DISPONIBLE PARA LA VENTA',62,1.00,6.00,50.00,50.00,50.00,9.00,59.00),(86,91,7,'PLATANO VERDE',44.00,5.00,39.00,1,'UNIDAD',2,'DISPONIBLE PARA LA VENTA',75,1.00,5.00,12.00,12.00,12.00,2.16,14.16);
/*!40000 ALTER TABLE `detalle_ajuste_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_entrada_inventario`
--

DROP TABLE IF EXISTS `detalle_entrada_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_entrada_inventario` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `entrada_inventario` int NOT NULL,
  `articulo` int NOT NULL,
  `descripcion_articulo` varchar(250) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `cantidad_recibida` decimal(12,2) DEFAULT NULL,
  `factor_conversion` decimal(12,2) DEFAULT NULL,
  `cantidad_fisica_base` decimal(12,2) DEFAULT NULL,
  `unidad` int DEFAULT NULL,
  `nombre_unidad` varchar(15) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `articulo_empaque` int DEFAULT NULL,
  `cantidad_pedida` decimal(12,2) DEFAULT NULL,
  `cantidad_pendiente` decimal(12,2) DEFAULT NULL,
  `precio_compra` decimal(12,2) DEFAULT NULL,
  `costo_unitario` decimal(12,2) DEFAULT NULL,
  `existencia_actual` decimal(12,2) DEFAULT NULL,
  `nueva_existencia` decimal(12,2) DEFAULT NULL,
  `precio_venta` decimal(12,2) DEFAULT NULL,
  `almacen` int DEFAULT NULL,
  `nombre_almacen` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `sub_total` decimal(12,2) DEFAULT NULL,
  `itbis` decimal(12,2) DEFAULT NULL,
  `total` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_det_entrada_inv_articulo` (`articulo`) USING BTREE,
  KEY `fk_det_entrada_inv_entrada_inv` (`entrada_inventario`) USING BTREE,
  KEY `fk_det_entrada_inv_unidad` (`unidad`) USING BTREE,
  KEY `detalle_entrada_inventario_ibfk_5` (`almacen`),
  KEY `fk_ent_det_inv_art_empq_idx` (`articulo_empaque`),
  CONSTRAINT `fk_det_ent_inv_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `fk_det_ent_inv_entrada` FOREIGN KEY (`entrada_inventario`) REFERENCES `entrada_inventario` (`codigo`),
  CONSTRAINT `fk_det_ent_inv_unidad` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`),
  CONSTRAINT `fk_ent_det_inv_almacen` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_ent_det_inv_art_empq` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=345 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_entrada_inventario`
--

LOCK TABLES `detalle_entrada_inventario` WRITE;
/*!40000 ALTER TABLE `detalle_entrada_inventario` DISABLE KEYS */;
INSERT INTO `detalle_entrada_inventario` VALUES (253,262,4,'SAZON RANCHERO',2.00,1.00,2.00,1,'UNIDAD',68,0.00,0.00,10.00,10.00,13.00,15.00,NULL,2,'DISPONIBLE PARA LA VENTA',5.40,30.00,35.40),(254,263,4,'SAZON RANCHERO',10.00,1.00,10.00,1,'UNIDAD',68,0.00,0.00,10.00,10.00,15.00,25.00,NULL,1,'ALMCEN  PRINCIPAL',18.00,100.00,118.00),(255,264,4,'SAZON RANCHERO',5.00,1.00,5.00,1,'UNIDAD',68,5.00,0.00,10.00,10.00,15.00,20.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL,NULL,NULL),(256,265,4,'SAZON RANCHERO',1.00,1.00,3.00,1,'UNIDAD',68,0.00,0.00,10.00,10.00,22.00,23.00,15.00,2,'DISPONIBLE PARA LA VENTA',8.10,45.00,53.10),(257,266,4,'SAZON RANCHERO',2.00,1.00,4.00,1,'UNIDAD',68,0.00,0.00,10.00,10.00,20.00,22.00,15.00,2,'DISPONIBLE PARA LA VENTA',10.80,60.00,70.80),(258,267,4,'SAZON RANCHERO',4.00,1.00,4.00,1,'UNIDAD',68,0.00,0.00,10.00,10.00,24.00,28.00,NULL,2,'DISPONIBLE PARA LA VENTA',10.80,60.00,70.80),(259,268,4,'SAZON RANCHERO',1.00,1.00,3.00,1,'UNIDAD',68,0.00,0.00,10.00,10.00,25.00,26.00,15.00,2,'DISPONIBLE PARA LA VENTA',8.10,45.00,53.10),(260,268,2,'LECHE LISTA MIL ILT',1.00,1.00,2.00,1,'UNIDAD',62,0.00,0.00,80.00,80.00,147.00,148.00,70.00,2,'DISPONIBLE PARA LA VENTA',25.20,140.00,165.20),(261,268,33,'ACEITE CRISOL UN LITRO',1.00,10.00,10.00,3,'CAJA',52,0.00,0.00,60.00,60.00,276.00,277.00,800.00,2,'DISPONIBLE PARA LA VENTA',144.00,800.00,944.00),(262,269,2,'LECHE LISTA MIL ILT',1.00,5.00,5.00,2,'PAQUETE',64,0.00,0.00,80.00,80.00,109.00,110.00,300.00,2,'DISPONIBLE PARA LA VENTA',54.00,300.00,354.00),(263,270,33,'ACEITE CRISOL UN LITRO',1.00,1.00,1.00,1,'UNIDAD',51,0.00,0.00,60.00,60.00,272.00,273.00,80.00,2,'DISPONIBLE PARA LA VENTA',14.40,80.00,94.40),(264,271,34,'ARROZ  LA GARZA DE  1LBS',2.00,10.00,20.00,7,'FALDO',67,0.00,0.00,25.00,25.00,19.00,21.00,NULL,1,'ALMCEN  PRINCIPAL',0.00,250.00,250.00),(265,272,34,'ARROZ  LA GARZA DE  1LBS',1.00,10.00,7.00,7,'FALDO',67,1.00,0.00,25.00,25.00,12.00,13.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL,NULL,NULL),(266,273,33,'ACEITE CRISOL UN LITRO',1.00,1.00,1.00,1,'UNIDAD',51,0.00,0.00,60.00,60.00,272.00,273.00,NULL,2,'DISPONIBLE PARA LA VENTA',14.40,80.00,94.40),(267,273,34,'ARROZ  LA GARZA DE  1LBS',3.00,1.00,3.00,1,'UNIDAD',66,0.00,0.00,25.00,25.00,26.00,29.00,NULL,2,'DISPONIBLE PARA LA VENTA',16.20,90.00,106.20),(268,274,33,'ACEITE CRISOL UN LITRO',4.00,1.00,4.00,1,'UNIDAD',51,0.00,0.00,60.00,60.00,263.00,267.00,NULL,2,'DISPONIBLE PARA LA VENTA',10.80,60.00,70.80),(269,275,33,'ACEITE CRISOL UN LITRO',1.00,10.00,273.00,3,'CAJA',52,1.00,0.00,60.00,60.00,0.00,1.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL,NULL,NULL),(270,276,33,'ACEITE CRISOL UN LITRO',1.00,10.00,10.00,3,'CAJA',52,0.00,0.00,60.00,60.00,257.00,258.00,800.00,2,'DISPONIBLE PARA LA VENTA',144.00,800.00,944.00),(271,277,2,'LECHE LISTA MIL ILT',1.00,1.00,3.00,1,'UNIDAD',62,0.00,0.00,80.00,80.00,112.00,113.00,70.00,2,'DISPONIBLE PARA LA VENTA',37.80,210.00,247.80),(272,278,2,'LECHE LISTA MIL ILT',1.00,1.00,3.00,1,'UNIDAD',62,0.00,0.00,80.00,80.00,110.00,111.00,70.00,2,'DISPONIBLE PARA LA VENTA',37.80,210.00,247.80),(273,279,33,'ACEITE CRISOL UN LITRO',2.00,10.00,253.00,3,'CAJA',52,2.00,0.00,60.00,60.00,8.00,10.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL,NULL,NULL),(274,280,2,'LECHE LISTA MIL ILT',1.00,1.00,4.00,1,'UNIDAD',62,0.00,0.00,80.00,80.00,107.00,108.00,70.00,2,'DISPONIBLE PARA LA VENTA',50.40,280.00,330.40),(275,280,33,'ACEITE CRISOL UN LITRO',1.00,1.00,3.00,1,'UNIDAD',51,0.00,0.00,60.00,60.00,262.00,263.00,80.00,2,'DISPONIBLE PARA LA VENTA',43.20,240.00,283.20),(276,281,2,'LECHE LISTA MIL ILT',2.00,1.00,2.00,1,'UNIDAD',62,0.00,0.00,80.00,80.00,106.00,108.00,70.00,2,'DISPONIBLE PARA LA VENTA',25.20,140.00,165.20),(277,282,1,'SALSA LINDA DE  1 KG',1.00,1.00,1.00,1,'UNIDAD',53,0.00,0.00,50.00,50.00,15.00,16.00,NULL,1,'ALMCEN  PRINCIPAL',25.00,50.00,75.00),(278,283,2,'LECHE LISTA MIL ILT',10.00,1.00,10.00,1,'UNIDAD',62,0.00,0.00,80.00,80.00,108.00,118.00,NULL,1,'ALMCEN  PRINCIPAL',640.00,800.00,1440.00),(279,283,34,'ARROZ  LA GARZA DE  1LBS',5.00,1.00,5.00,1,'UNIDAD',66,0.00,0.00,25.00,25.00,29.00,34.00,NULL,1,'ALMCEN  PRINCIPAL',31.25,125.00,156.25),(280,284,6,'ARROZ PINCO DE 1LB',2.00,1.00,2.00,1,'UNIDAD',69,0.00,0.00,25.00,25.00,12.00,14.00,NULL,1,'ALMCEN  PRINCIPAL',2700.00,15000.00,17700.00),(281,285,6,'ARROZ PINCO DE 1LB',2.00,1.00,2.00,1,'UNIDAD',69,0.00,0.00,25.00,25.00,14.00,16.00,NULL,1,'ALMCEN  PRINCIPAL',2700.00,15000.00,17700.00),(282,286,6,'ARROZ PINCO DE 1LB',2.00,1.00,2.00,1,'UNIDAD',69,0.00,0.00,25.00,25.00,16.00,18.00,NULL,1,'ALMCEN  PRINCIPAL',2700.00,15000.00,17700.00),(283,287,2,'LECHE LISTA MIL ILT',10.00,1.00,10.00,1,'UNIDAD',62,0.00,0.00,80.00,80.00,118.00,128.00,NULL,1,'ALMCEN  PRINCIPAL',640.00,800.00,1440.00),(284,287,34,'ARROZ  LA GARZA DE  1LBS',5.00,1.00,5.00,1,'UNIDAD',66,0.00,0.00,25.00,25.00,34.00,39.00,NULL,1,'ALMCEN  PRINCIPAL',31.25,125.00,156.25),(318,303,2,'LECHE LISTA MIL ILT',3.00,1.00,3.00,1,'UNIDAD',62,0.00,0.00,80.00,80.00,128.00,131.00,NULL,1,'ALMCEN  PRINCIPAL',270.00,1500.00,1770.00),(319,304,2,'LECHE LISTA MIL ILT',1.00,1.00,1.00,1,'UNIDAD',62,0.00,0.00,80.00,80.00,131.00,132.00,NULL,1,'ALMCEN  PRINCIPAL',90.00,500.00,590.00),(320,305,1,'SALSA LINDA DE  1 KG',1.00,1.00,1.00,1,'UNIDAD',53,0.00,0.00,50.00,50.00,16.00,17.00,NULL,1,'ALMCEN  PRINCIPAL',25.00,50.00,75.00),(321,305,6,'ARROZ PINCO DE 1LB',10.00,1.00,10.00,1,'UNIDAD',69,0.00,0.00,25.00,25.00,18.00,28.00,NULL,1,'ALMCEN  PRINCIPAL',62.50,250.00,312.50),(322,306,1,'SALSA LINDA DE  1 KG',10.00,1.00,10.00,1,'UNIDAD',53,0.00,0.00,50.00,50.00,17.00,27.00,NULL,1,'ALMCEN  PRINCIPAL',90.00,500.00,590.00),(323,306,4,'SAZON RANCHERO',3.00,1.00,3.00,1,'UNIDAD',68,0.00,0.00,10.00,10.00,22.00,25.00,NULL,1,'ALMCEN  PRINCIPAL',5.40,30.00,35.40),(324,306,6,'ARROZ PINCO DE 1LB',3.00,300.00,900.00,5,'SACO',70,0.00,0.00,25.00,25.00,28.00,31.00,NULL,1,'ALMCEN  PRINCIPAL',4050.00,22500.00,26550.00),(325,307,6,'ARROZ PINCO DE 1LB',1.00,300.00,300.00,5,'SACO',70,0.00,0.00,25.00,25.00,928.00,929.00,NULL,1,'ALMCEN  PRINCIPAL',1350.00,7500.00,8850.00),(326,308,33,'ACEITE CRISOL UN LITRO',2.00,10.00,20.00,3,'CAJA',52,0.00,0.00,60.00,60.00,261.00,263.00,NULL,1,'ALMCEN  PRINCIPAL',216.00,1200.00,1416.00),(327,309,1,'SALSA LINDA DE  1 KG',1.00,1.00,1.00,1,'UNIDAD',53,0.00,0.00,50.00,50.00,27.00,28.00,NULL,1,'ALMCEN  PRINCIPAL',9.00,50.00,59.00),(328,309,4,'SAZON RANCHERO',1.00,1.00,1.00,1,'UNIDAD',68,0.00,0.00,10.00,10.00,25.00,26.00,NULL,1,'ALMCEN  PRINCIPAL',1.80,10.00,11.80),(329,309,6,'ARROZ PINCO DE 1LB',1.00,300.00,300.00,5,'SACO',70,0.00,0.00,25.00,25.00,1228.00,1229.00,NULL,1,'ALMCEN  PRINCIPAL',1350.00,7500.00,8850.00),(330,310,2,'LECHE LISTA MIL ILT',1.00,10.00,10.00,3,'CAJA',63,0.00,0.00,80.00,80.00,132.00,133.00,NULL,1,'ALMCEN  PRINCIPAL',90.00,500.00,590.00),(331,311,2,'LECHE LISTA MIL ILT',2.00,1.00,2.00,1,'UNIDAD',62,0.00,0.00,80.00,80.00,142.00,144.00,NULL,1,'ALMCEN  PRINCIPAL',18.00,100.00,118.00),(332,312,34,'ARROZ  LA GARZA DE  1LBS',1.00,10.00,7.00,7,'FALDO',67,1.00,0.00,25.00,25.00,22.00,23.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL,NULL,NULL),(333,313,5,'LECHE NIDO DE UN LT',3.00,15.00,45.00,3,'CAJA',72,0.00,0.00,35.00,35.00,0.00,3.00,NULL,1,'ALMCEN  PRINCIPAL',283.50,1575.00,1858.50),(334,314,5,'LECHE NIDO DE UN LT',1.00,15.00,30.00,3,'CAJA',72,1.00,0.00,35.00,35.00,0.00,1.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL,NULL,NULL),(335,315,33,'ACEITE CRISOL ',1.00,10.00,263.00,3,'CAJA',52,1.00,0.00,60.00,60.00,-8.00,-7.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL,NULL,NULL),(336,316,3,'SOPITA DOÃ‘A GALLINA',100.00,1.00,100.00,6,'SOBRE',73,0.00,0.00,10.00,10.00,-44.00,56.00,NULL,2,'DISPONIBLE PARA LA VENTA',1.80,10.00,11.80),(337,317,2,'LECHE RICA LISTA MIL ',1.00,10.00,118.00,3,'CAJA',63,1.00,0.00,80.00,80.00,6.00,7.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL,NULL,NULL),(338,318,2,'LECHE RICA LISTA MIL ',3.00,10.00,88.00,3,'CAJA',63,3.00,0.00,80.00,80.00,6.00,9.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL,NULL,NULL),(339,319,3,'SOPITA DOÃ‘A GALLINA',2.00,1.00,2.00,6,'SOBRE',73,2.00,0.00,10.00,10.00,55.00,57.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL,NULL,NULL),(340,320,2,'LECHE RICA LISTA MIL ',2.00,5.00,10.00,2,'PAQUETE',64,0.00,0.00,80.00,80.00,93.00,95.00,NULL,2,'DISPONIBLE PARA LA VENTA',108.00,600.00,708.00),(341,321,1,'SALSA LINDA DE  1 KG',1.00,1.00,1.00,1,'UNIDAD',53,0.00,0.00,50.00,50.00,27.00,28.00,60.00,2,'DISPONIBLE PARA LA VENTA',10.80,60.00,70.80),(342,322,2,'LECHE RICA LISTA MIL ',2.00,10.00,68.00,3,'CAJA',63,2.00,0.00,80.00,80.00,0.00,2.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL,NULL,NULL),(343,323,7,'PLATANO VERDE',50.00,1.00,50.00,1,'UNIDAD',75,0.00,0.00,15.00,15.00,0.00,50.00,NULL,1,'ALMCEN  PRINCIPAL',135.00,750.00,885.00),(344,324,7,'PLATANO VERDE',50.00,1.00,0.00,1,'UNIDAD',75,50.00,0.00,12.00,12.00,0.00,50.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL,NULL,NULL);
/*!40000 ALTER TABLE `detalle_entrada_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_factura_de_venta`
--

DROP TABLE IF EXISTS `detalle_factura_de_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_factura_de_venta` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `factura` int NOT NULL,
  `articulo` int NOT NULL,
  `descripcion_articulo` varchar(250) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `unidad` int DEFAULT NULL,
  `nombre_unidad` varchar(10) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `articulo_empaque` int DEFAULT NULL,
  `factor_conversion` decimal(12,2) DEFAULT NULL,
  `cantidad_fisica_base` decimal(12,2) DEFAULT NULL,
  `cantidad` decimal(12,2) NOT NULL,
  `precio_compra` decimal(12,2) DEFAULT NULL,
  `existencia_actual` decimal(12,2) DEFAULT NULL,
  `nueva_existencia` decimal(12,2) DEFAULT NULL,
  `almacen` int DEFAULT NULL,
  `nombre_almacen` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `precio_venta` decimal(12,2) DEFAULT NULL,
  `sub_total` decimal(12,2) NOT NULL,
  `itbis` decimal(12,2) NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `total_descuento` decimal(12,2) NOT NULL,
  `porciento_descuento` decimal(12,2) NOT NULL,
  `porciento_itbis` decimal(12,2) NOT NULL,
  `numero_de_linea` int NOT NULL,
  `cantidad_devuelta` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `detalle_entrada_inventario_ibfk_5` (`almacen`) USING BTREE,
  KEY `fk_det_entrada_inv_articulo` (`articulo`) USING BTREE,
  KEY `fk_det_entrada_inv_entrada_inv` (`factura`) USING BTREE,
  KEY `fk_det_entrada_inv_unidad` (`unidad`) USING BTREE,
  KEY `fk_det_fact_art_empa_idx` (`articulo_empaque`),
  CONSTRAINT `fk_det_ent_inv_articulo_copy` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_det_ent_inv_unidad_copy` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_det_fact_art_empaq` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_det_fact_art_empq` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_det_fact_factura` FOREIGN KEY (`factura`) REFERENCES `factura_de_venta` (`codigo`),
  CONSTRAINT `fk_ent_det_inv_almacen_copy` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=150 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_factura_de_venta`
--

LOCK TABLES `detalle_factura_de_venta` WRITE;
/*!40000 ALTER TABLE `detalle_factura_de_venta` DISABLE KEYS */;
INSERT INTO `detalle_factura_de_venta` VALUES (65,142,4,'SAZON RANCHERO',1,'UNIDAD',68,1.00,2.00,2.00,10.00,15.00,NULL,2,'DISPONIBLE PARA LA VENTA',15.00,30.00,5.40,35.40,0.00,0.00,18.00,1,0.00),(66,143,4,'SAZON RANCHERO',1,'UNIDAD',68,1.00,3.00,3.00,10.00,13.00,NULL,2,'DISPONIBLE PARA LA VENTA',15.00,45.00,8.10,53.10,0.00,0.00,18.00,1,1.00),(67,144,4,'SAZON RANCHERO',1,'UNIDAD',68,1.00,4.00,4.00,10.00,22.00,NULL,2,'DISPONIBLE PARA LA VENTA',15.00,60.00,10.80,70.80,0.00,0.00,18.00,1,2.00),(68,145,2,'LECHE LISTA MIL ILT',1,'UNIDAD',62,1.00,3.00,3.00,80.00,152.00,NULL,2,'DISPONIBLE PARA LA VENTA',70.00,210.00,37.80,247.80,0.00,0.00,18.00,1,0.00),(69,146,4,'SAZON RANCHERO',1,'UNIDAD',68,1.00,3.00,3.00,10.00,28.00,NULL,2,'DISPONIBLE PARA LA VENTA',15.00,45.00,8.10,53.10,0.00,0.00,18.00,2,1.00),(70,146,2,'LECHE LISTA MIL ILT',1,'UNIDAD',62,1.00,2.00,2.00,80.00,152.00,NULL,2,'DISPONIBLE PARA LA VENTA',70.00,140.00,25.20,165.20,0.00,0.00,18.00,4,1.00),(71,146,33,'ACEITE CRISOL UN LITRO',3,'CAJA',52,10.00,10.00,1.00,60.00,286.00,NULL,2,'DISPONIBLE PARA LA VENTA',800.00,800.00,144.00,944.00,0.00,0.00,18.00,5,1.00),(72,147,2,'LECHE LISTA MIL ILT',3,'CAJA',63,10.00,10.00,1.00,80.00,152.00,NULL,2,'DISPONIBLE PARA LA VENTA',700.00,700.00,126.00,826.00,0.00,0.00,18.00,8,0.00),(73,147,2,'LECHE LISTA MIL ILT',2,'PAQUETE',64,5.00,5.00,1.00,80.00,152.00,NULL,2,'DISPONIBLE PARA LA VENTA',300.00,300.00,54.00,354.00,0.00,0.00,18.00,9,0.00),(74,148,33,'ACEITE CRISOL UN LITRO',3,'CAJA',52,10.00,10.00,1.00,60.00,286.00,NULL,2,'DISPONIBLE PARA LA VENTA',800.00,800.00,144.00,944.00,0.00,0.00,18.00,7,0.00),(75,148,4,'SAZON RANCHERO',1,'UNIDAD',68,1.00,1.00,1.00,10.00,28.00,NULL,2,'DISPONIBLE PARA LA VENTA',15.00,15.00,2.70,17.70,0.00,0.00,18.00,10,0.00),(76,148,34,'ARROZ  PINCO DE  1LBS',1,'UNIDAD',66,1.00,1.00,1.00,25.00,20.00,NULL,2,'DISPONIBLE PARA LA VENTA',30.00,30.00,5.40,35.40,0.00,0.00,18.00,11,0.00),(77,148,2,'LECHE LISTA MIL ILT',3,'CAJA',63,10.00,10.00,1.00,80.00,152.00,NULL,2,'DISPONIBLE PARA LA VENTA',700.00,700.00,126.00,826.00,0.00,0.00,18.00,12,0.00),(78,148,33,'ACEITE CRISOL UN LITRO',1,'UNIDAD',51,1.00,1.00,1.00,60.00,286.00,NULL,2,'DISPONIBLE PARA LA VENTA',80.00,80.00,14.40,94.40,0.00,0.00,18.00,13,0.00),(79,149,33,'ACEITE CRISOL UN LITRO',1,'UNIDAD',51,1.00,2.00,2.00,60.00,275.00,NULL,2,'DISPONIBLE PARA LA VENTA',80.00,160.00,28.80,188.80,0.00,0.00,18.00,1,0.00),(80,149,2,'LECHE LISTA MIL ILT',3,'CAJA',63,10.00,10.00,1.00,80.00,124.00,NULL,2,'DISPONIBLE PARA LA VENTA',700.00,700.00,126.00,826.00,0.00,0.00,18.00,2,0.00),(81,149,2,'LECHE LISTA MIL ILT',2,'PAQUETE',64,5.00,5.00,1.00,80.00,124.00,NULL,2,'DISPONIBLE PARA LA VENTA',300.00,300.00,54.00,354.00,0.00,0.00,18.00,3,1.00),(82,150,33,'ACEITE CRISOL UN LITRO',1,'UNIDAD',51,1.00,1.00,1.00,60.00,273.00,NULL,2,'DISPONIBLE PARA LA VENTA',80.00,80.00,14.40,94.40,0.00,0.00,18.00,1,1.00),(83,150,4,'SAZON RANCHERO',1,'UNIDAD',68,1.00,2.00,2.00,10.00,26.00,NULL,2,'DISPONIBLE PARA LA VENTA',15.00,30.00,5.40,35.40,0.00,0.00,18.00,2,0.00),(86,152,33,'ACEITE CRISOL UN LITRO',1,'UNIDAD',51,1.00,1.00,1.00,60.00,273.00,NULL,2,'DISPONIBLE PARA LA VENTA',80.00,80.00,14.40,94.40,0.00,0.00,18.00,1,0.00),(87,152,34,'ARROZ  LA GARZA DE  1LBS',1,'UNIDAD',66,1.00,3.00,3.00,25.00,29.00,NULL,2,'DISPONIBLE PARA LA VENTA',30.00,90.00,16.20,106.20,0.00,0.00,18.00,2,0.00),(88,153,33,'ACEITE CRISOL UN LITRO',3,'CAJA',52,10.00,10.00,1.00,60.00,273.00,NULL,2,'DISPONIBLE PARA LA VENTA',800.00,800.00,144.00,944.00,0.00,0.00,18.00,1,0.00),(89,154,33,'ACEITE CRISOL UN LITRO',3,'CAJA',52,10.00,10.00,1.00,60.00,267.00,NULL,2,'DISPONIBLE PARA LA VENTA',800.00,800.00,144.00,944.00,0.00,0.00,18.00,1,1.00),(90,154,2,'LECHE LISTA MIL ILT',1,'UNIDAD',62,1.00,3.00,3.00,80.00,115.00,NULL,2,'DISPONIBLE PARA LA VENTA',70.00,210.00,37.80,247.80,0.00,0.00,18.00,4,1.00),(91,155,33,'ACEITE CRISOL UN LITRO',1,'UNIDAD',51,1.00,2.00,2.00,60.00,267.00,NULL,2,'DISPONIBLE PARA LA VENTA',80.00,160.00,28.80,188.80,0.00,0.00,18.00,1,0.00),(92,156,2,'LECHE LISTA MIL ILT',1,'UNIDAD',62,1.00,3.00,3.00,80.00,112.00,NULL,2,'DISPONIBLE PARA LA VENTA',70.00,210.00,37.80,247.80,0.00,0.00,18.00,3,1.00),(93,157,2,'LECHE LISTA MIL ILT',1,'UNIDAD',62,1.00,4.00,4.00,80.00,112.00,NULL,2,'DISPONIBLE PARA LA VENTA',70.00,280.00,50.40,330.40,0.00,0.00,18.00,4,1.00),(94,157,33,'ACEITE CRISOL UN LITRO',1,'UNIDAD',51,1.00,3.00,3.00,60.00,267.00,NULL,2,'DISPONIBLE PARA LA VENTA',80.00,240.00,43.20,283.20,0.00,0.00,18.00,5,1.00),(95,158,33,'ACEITE CRISOL UN LITRO',1,'UNIDAD',51,1.00,2.00,2.00,60.00,263.00,NULL,2,'DISPONIBLE PARA LA VENTA',80.00,160.00,28.80,188.80,0.00,0.00,18.00,2,0.00),(96,158,2,'LECHE LISTA MIL ILT',1,'UNIDAD',62,1.00,2.00,2.00,80.00,108.00,NULL,2,'DISPONIBLE PARA LA VENTA',70.00,140.00,25.20,165.20,0.00,0.00,18.00,3,2.00),(97,158,4,'SAZON RANCHERO',1,'UNIDAD',68,1.00,2.00,2.00,10.00,24.00,NULL,2,'DISPONIBLE PARA LA VENTA',15.00,30.00,5.40,35.40,0.00,0.00,18.00,4,0.00),(98,159,33,'ACEITE CRISOL UN LITRO',1,'UNIDAD',51,1.00,2.00,2.00,60.00,281.00,NULL,2,'DISPONIBLE PARA LA VENTA',80.00,160.00,28.80,188.80,0.00,0.00,18.00,1,0.00),(99,160,5,'LECHE NIDO DE UN LT',1,'UNIDAD',71,1.00,2.00,2.00,35.00,45.00,NULL,2,'DISPONIBLE PARA LA VENTA',45.00,90.00,16.20,106.20,0.00,0.00,18.00,1,0.00),(101,162,33,'ACEITE CRISOL UN LITRO',3,'CAJA',52,10.00,30.00,3.00,60.00,279.00,NULL,2,'DISPONIBLE PARA LA VENTA',800.00,2400.00,432.00,2832.00,0.00,0.00,18.00,3,0.00),(104,164,33,'ACEITE CRISOL  UNIDAD',1,'UNIDAD',51,1.00,1.00,1.00,60.00,249.00,NULL,2,'DISPONIBLE PARA LA VENTA',80.00,80.00,14.40,94.40,0.00,0.00,18.00,1,0.00),(105,164,33,'ACEITE CRISOL CAJA',3,'CAJA',52,10.00,10.00,1.00,60.00,249.00,NULL,2,'DISPONIBLE PARA LA VENTA',800.00,800.00,144.00,944.00,0.00,0.00,18.00,2,0.00),(108,166,3,'SOPITA DOÃ‘A GALLINA CAJA DE 48',3,'CAJA',74,48.00,48.00,1.00,10.00,4.00,NULL,2,'DISPONIBLE PARA LA VENTA',720.00,720.00,129.60,849.60,0.00,0.00,18.00,1,0.00),(110,168,3,'SOPITA DOÃ‘A GALLINA SOBRE 1GRM',6,'SOBRE',73,1.00,2.00,2.00,10.00,56.00,NULL,2,'DISPONIBLE PARA LA VENTA',15.00,30.00,5.40,35.40,0.00,0.00,18.00,1,0.00),(111,169,2,'LECHE LISTA MIL ILT CAJA',3,'CAJA',63,10.00,10.00,1.00,80.00,144.00,NULL,2,'DISPONIBLE PARA LA VENTA',700.00,700.00,126.00,826.00,0.00,0.00,18.00,1,0.00),(112,170,2,'LECHE LISTA MIL ILT CAJA',3,'CAJA',63,10.00,10.00,1.00,80.00,144.00,NULL,2,'DISPONIBLE PARA LA VENTA',700.00,700.00,126.00,826.00,0.00,0.00,18.00,5,0.00),(116,172,2,'LECHE RICA LISTA MIL  CAJA DE 20',3,'CAJA',63,10.00,10.00,1.00,80.00,124.00,NULL,2,'DISPONIBLE PARA LA VENTA',700.00,700.00,126.00,826.00,0.00,0.00,18.00,1,0.00),(117,173,2,'LECHE LISTA MIL ILT UNIDAD',1,'UNIDAD',62,1.00,3.00,3.00,80.00,114.00,NULL,2,'DISPONIBLE PARA LA VENTA',70.00,210.00,37.80,247.80,0.00,0.00,18.00,4,0.00),(118,174,2,'LECHE RICA LISTA MIL UNIDAD',1,'UNIDAD',62,1.00,1.00,1.00,80.00,105.00,NULL,2,'DISPONIBLE PARA LA VENTA',70.00,70.00,12.60,82.60,0.00,0.00,18.00,1,0.00),(119,175,2,'LECHE RICA LISTA MIL  CAJITA DE 1L',1,'UNIDAD',62,1.00,1.00,1.00,80.00,104.00,NULL,2,'DISPONIBLE PARA LA VENTA',70.00,70.00,12.60,82.60,0.00,0.00,18.00,1,0.00),(120,176,2,'LECHE RICA LISTA MIL  PAQUETE DE  5',2,'PAQUETE',64,5.00,10.00,2.00,80.00,103.00,NULL,2,'DISPONIBLE PARA LA VENTA',300.00,600.00,108.00,708.00,0.00,0.00,18.00,2,0.00),(123,179,1,'SALSA LINDA DE  1 KG UNIDAD',1,'UNIDAD',53,1.00,1.00,1.00,50.00,28.00,NULL,2,'DISPONIBLE PARA LA VENTA',60.00,60.00,10.80,70.80,0.00,0.00,18.00,5,1.00),(127,181,2,'LECHE RICA LISTA MIL CAJA DE 20',3,'CAJA',63,10.00,10.00,1.00,80.00,103.00,NULL,2,'DISPONIBLE PARA LA VENTA',700.00,700.00,126.00,826.00,0.00,0.00,18.00,4,0.00),(131,183,3,'SOPITA DOÃ‘A GALLINA SOBRE',6,'SOBRE',73,1.00,1.00,1.00,10.00,54.00,NULL,2,'DISPONIBLE PARA LA VENTA',15.00,15.00,2.70,17.70,0.00,0.00,18.00,5,0.00),(134,185,3,'SOPITA DOÃ‘A GALLINA SOBRE',6,'SOBRE',73,1.00,2.00,2.00,10.00,53.00,NULL,2,'DISPONIBLE PARA LA VENTA',15.00,30.00,5.40,35.40,0.00,0.00,18.00,4,0.00),(137,187,5,'LECHE NIDO DE UN LT UNIDAD',1,'UNIDAD',71,1.00,2.00,2.00,35.00,43.00,NULL,2,'DISPONIBLE PARA LA VENTA',45.00,90.00,16.20,106.20,0.00,0.00,18.00,3,0.00),(140,189,2,'LECHE RICA LISTA MIL  PAQUETE DE  5',2,'PAQUETE',64,5.00,5.00,1.00,80.00,93.00,NULL,2,'DISPONIBLE PARA LA VENTA',300.00,300.00,54.00,354.00,0.00,0.00,18.00,5,0.00),(145,193,3,'SOPITA DOÃ‘A GALLINA CAJA',3,'CAJA',74,48.00,48.00,1.00,10.00,51.00,NULL,2,'DISPONIBLE PARA LA VENTA',720.00,720.00,129.60,849.60,0.00,0.00,18.00,6,0.00),(146,194,5,'LECHE NIDO DE UN LT UNIDAD',1,'UNIDAD',71,1.00,2.00,2.00,35.00,41.00,NULL,2,'DISPONIBLE PARA LA VENTA',45.00,90.00,16.20,106.20,0.00,0.00,18.00,1,0.00),(147,195,2,'LECHE RICA LISTA MIL  CAJITA DE 1L',1,'UNIDAD',62,1.00,4.00,4.00,80.00,88.00,NULL,2,'DISPONIBLE PARA LA VENTA',70.00,280.00,50.40,330.40,0.00,0.00,18.00,3,0.00),(149,197,7,'PLATANO VERDE UNIDAD',1,'UNIDAD',75,1.00,6.00,6.00,12.00,50.00,NULL,2,'DISPONIBLE PARA LA VENTA',20.00,120.00,21.60,141.60,0.00,0.00,18.00,1,0.00);
/*!40000 ALTER TABLE `detalle_factura_de_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_merma`
--

DROP TABLE IF EXISTS `detalle_merma`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_merma` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `merma` int NOT NULL,
  `articulo` int NOT NULL,
  `articulo_empaque` int DEFAULT NULL,
  `descripcion_articulo` varchar(255) DEFAULT NULL,
  `cantidad` decimal(18,4) NOT NULL,
  `factor_conversion` decimal(18,4) NOT NULL DEFAULT '1.0000',
  `nombre_unidad` varchar(100) DEFAULT NULL,
  `costo_unitario` decimal(18,4) NOT NULL DEFAULT '0.0000',
  `total` decimal(18,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`codigo`),
  KEY `fk_detalle_merma_merma` (`merma`),
  KEY `fk_detalle_merma_articulo` (`articulo`),
  KEY `fk_detalle_merma_articulo_empaque` (`articulo_empaque`),
  CONSTRAINT `fk_detalle_merma_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `fk_detalle_merma_articulo_empaque` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_detalle_merma_merma` FOREIGN KEY (`merma`) REFERENCES `merma` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_merma`
--

LOCK TABLES `detalle_merma` WRITE;
/*!40000 ALTER TABLE `detalle_merma` DISABLE KEYS */;
INSERT INTO `detalle_merma` VALUES (1,2,4,68,'SAZON RANCHERO',1.0000,1.0000,'UNIDAD',10.0000,10.00),(2,3,7,75,'PLATANO VERDE',5.0000,1.0000,'UNIDAD',12.0000,60.00),(3,4,7,75,'PLATANO VERDE',2.0000,1.0000,'UNIDAD',12.0000,24.00);
/*!40000 ALTER TABLE `detalle_merma` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_ordende_de_compra`
--

DROP TABLE IF EXISTS `detalle_ordende_de_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_ordende_de_compra` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `orden_de_compra` int NOT NULL,
  `articulo` int DEFAULT NULL,
  `descripcion_articulo` varchar(80) DEFAULT NULL,
  `unidad` int DEFAULT NULL,
  `precio_unitario` decimal(12,2) DEFAULT NULL,
  `cantidad` decimal(12,2) DEFAULT NULL,
  `cantidad_recibida` decimal(12,2) DEFAULT NULL,
  `impuesto` decimal(12,2) DEFAULT NULL,
  `descuento` decimal(12,2) DEFAULT NULL,
  `sub_total` decimal(12,2) DEFAULT NULL,
  `total` decimal(12,2) DEFAULT NULL,
  `pendiente` decimal(12,2) DEFAULT NULL,
  `otros_impuesto` decimal(12,2) DEFAULT NULL,
  `itbis` decimal(12,2) DEFAULT NULL,
  `isr` decimal(12,2) DEFAULT NULL,
  `articulo_empaque` int DEFAULT NULL,
  `factor_conversion` decimal(12,2) DEFAULT NULL,
  `cantidad_fisica_base` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_detalle_orden_compra_orden_compra_idx` (`orden_de_compra`) USING BTREE,
  KEY `fk_detalle_orden__articulo_idx` (`articulo`) USING BTREE,
  KEY `fk_detalle_orden_art_empq_idx` (`articulo_empaque`),
  KEY `fk_detalle_orden_unidad_idx` (`unidad`),
  CONSTRAINT `fk_detalle_orden_art_empq` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_detalle_orden_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `fk_detalle_orden_compra_orden_compra` FOREIGN KEY (`orden_de_compra`) REFERENCES `orden_de_compra` (`codigo`),
  CONSTRAINT `fk_detalle_orden_unidad` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=785 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_ordende_de_compra`
--

LOCK TABLES `detalle_ordende_de_compra` WRITE;
/*!40000 ALTER TABLE `detalle_ordende_de_compra` DISABLE KEYS */;
INSERT INTO `detalle_ordende_de_compra` VALUES (767,10,2,'LECHE LISTA MIL ILT',NULL,80.00,10.00,0.00,NULL,NULL,800.00,1440.00,10.00,NULL,640.00,NULL,NULL,NULL,NULL),(768,10,34,'ARROZ  LA GARZA DE  1LBS',NULL,25.00,5.00,0.00,NULL,NULL,125.00,156.25,5.00,NULL,31.25,NULL,NULL,NULL,NULL),(769,11,1,'SALSA LINDA DE  1 KG',NULL,50.00,1.00,1.00,NULL,NULL,50.00,75.00,0.00,NULL,25.00,NULL,NULL,NULL,NULL),(770,11,6,'ARROZ PINCO DE 1LB',NULL,25.00,10.00,10.00,NULL,NULL,250.00,312.50,0.00,NULL,62.50,NULL,NULL,NULL,NULL),(771,11,8,'QUESO CHEDA  LB',NULL,150.00,1.00,0.00,NULL,NULL,150.00,375.00,1.00,NULL,225.00,NULL,NULL,NULL,NULL),(772,12,6,'ARROZ PINCO DE 1LB',NULL,7500.00,2.00,0.00,NULL,NULL,15000.00,17700.00,2.00,NULL,2700.00,NULL,70,300.00,600.00),(773,13,1,'SALSA LINDA DE  1 KG',NULL,50.00,10.00,10.00,NULL,NULL,500.00,590.00,0.00,NULL,90.00,NULL,53,1.00,10.00),(774,13,4,'SAZON RANCHERO',NULL,10.00,3.00,3.00,NULL,NULL,30.00,35.40,0.00,NULL,5.40,NULL,68,1.00,3.00),(775,13,6,'ARROZ PINCO DE 1LB',NULL,7500.00,3.00,3.00,NULL,NULL,22500.00,26550.00,0.00,NULL,4050.00,NULL,70,300.00,900.00),(776,18,2,'LECHE LISTA MIL ILT',NULL,500.00,1.00,1.00,NULL,NULL,500.00,590.00,0.00,NULL,90.00,NULL,63,10.00,10.00),(777,20,2,'LECHE LISTA MIL ILT',3,500.00,3.00,3.00,NULL,NULL,1500.00,1770.00,0.00,NULL,270.00,NULL,63,10.00,30.00),(778,21,6,'ARROZ PINCO DE 1LB',5,7500.00,1.00,1.00,NULL,NULL,7500.00,8850.00,0.00,NULL,1350.00,NULL,70,300.00,300.00),(779,22,1,'SALSA LINDA DE  1 KG',1,50.00,1.00,1.00,NULL,NULL,50.00,59.00,0.00,NULL,9.00,NULL,53,1.00,1.00),(780,22,4,'SAZON RANCHERO',1,10.00,1.00,1.00,NULL,NULL,10.00,11.80,0.00,NULL,1.80,NULL,68,1.00,1.00),(781,22,6,'ARROZ PINCO DE 1LB',5,7500.00,2.00,1.00,NULL,NULL,15000.00,17700.00,1.00,NULL,2700.00,NULL,70,300.00,600.00),(782,23,2,'LECHE LISTA MIL ILT',3,500.00,3.00,1.00,NULL,NULL,1500.00,1770.00,2.00,NULL,270.00,NULL,63,10.00,30.00),(783,24,2,'LECHE LISTA MIL ILT',1,50.00,2.00,2.00,NULL,NULL,100.00,118.00,0.00,NULL,18.00,NULL,62,1.00,2.00),(784,25,5,'LECHE NIDO DE UN LT',3,525.00,3.00,3.00,NULL,NULL,1575.00,1858.50,0.00,NULL,283.50,NULL,72,15.00,45.00);
/*!40000 ALTER TABLE `detalle_ordende_de_compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_recepcion_mercancia`
--

DROP TABLE IF EXISTS `detalle_recepcion_mercancia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_recepcion_mercancia` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `recepcion_mercancia` int NOT NULL,
  `articulo` int NOT NULL,
  `descripcion_articulo` varchar(255) NOT NULL,
  `nombre_unidad` varchar(20) NOT NULL DEFAULT 'UNIDAD',
  `unidad` int NOT NULL,
  `cantidad_recibida` decimal(12,2) NOT NULL,
  `precio_unitario` decimal(12,2) NOT NULL,
  `sub_total` decimal(12,2) NOT NULL,
  `itbis` decimal(12,2) NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `articulo_empaque` int DEFAULT NULL,
  `factor_conversion` decimal(12,2) DEFAULT NULL,
  `cantidad_fisica_base` decimal(12,2) DEFAULT NULL,
  `almacen` int DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_det_rec_articulo` (`articulo`),
  KEY `fk_det_recepcion_mercancia_idx` (`codigo`,`recepcion_mercancia`),
  KEY `fk_det_recepcion_unidad_idx` (`unidad`),
  KEY `fk_det_recepcion_recepcion_idx` (`recepcion_mercancia`),
  KEY `fk_det_recepcion_art_empaque_idx` (`articulo_empaque`),
  KEY `fk_det_recepcion_alm_idx` (`almacen`),
  CONSTRAINT `fk_det_recepcion_alm` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_det_recepcion_art_empaque` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_det_recepcion_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `fk_det_recepcion_recepcion` FOREIGN KEY (`recepcion_mercancia`) REFERENCES `recepcion_mercancia` (`codigo`),
  CONSTRAINT `fk_det_recepcion_unidad` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=112 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_recepcion_mercancia`
--

LOCK TABLES `detalle_recepcion_mercancia` WRITE;
/*!40000 ALTER TABLE `detalle_recepcion_mercancia` DISABLE KEYS */;
INSERT INTO `detalle_recepcion_mercancia` VALUES (44,53,2,'LECHE LISTA MIL ILT','CAJA',3,3.00,500.00,1500.00,270.00,1770.00,63,10.00,30.00,1),(45,54,2,'LECHE LISTA MIL ILT','UNIDAD',1,10.00,50.00,500.00,90.00,590.00,62,1.00,10.00,2),(46,55,33,'ACEITE CRISOL UN LITRO','CAJA',3,2.00,600.00,1200.00,216.00,1416.00,52,10.00,20.00,2),(47,56,4,'SAZON RANCHERO','UNIDAD',1,10.00,10.00,100.00,18.00,118.00,68,1.00,10.00,1),(50,59,1,'SALSA LINDA DE  1 KG','UNIDAD',1,1.00,50.00,50.00,25.00,75.00,53,1.00,1.00,1),(51,60,2,'LECHE LISTA MIL ILT','UNIDAD',1,10.00,80.00,800.00,640.00,1440.00,62,1.00,10.00,1),(52,60,34,'ARROZ  LA GARZA DE  1LBS','UNIDAD',1,5.00,25.00,125.00,31.25,156.25,66,1.00,5.00,1),(54,62,6,'ARROZ PINCO DE 1LB','UNIDAD',1,2.00,7500.00,15000.00,2700.00,17700.00,69,1.00,2.00,1),(55,63,6,'ARROZ PINCO DE 1LB','UNIDAD',1,2.00,7500.00,15000.00,2700.00,17700.00,69,1.00,2.00,1),(58,66,6,'ARROZ PINCO DE 1LB','UNIDAD',1,2.00,7500.00,15000.00,2700.00,17700.00,69,1.00,2.00,1),(59,67,2,'LECHE LISTA MIL ILT','UNIDAD',1,10.00,80.00,800.00,640.00,1440.00,62,1.00,10.00,1),(60,67,34,'ARROZ  LA GARZA DE  1LBS','UNIDAD',1,5.00,25.00,125.00,31.25,156.25,66,1.00,5.00,1),(94,83,2,'LECHE LISTA MIL ILT','UNIDAD',1,3.00,500.00,1500.00,270.00,1770.00,62,1.00,3.00,1),(95,84,2,'LECHE LISTA MIL ILT','UNIDAD',1,1.00,500.00,500.00,90.00,590.00,62,1.00,1.00,1),(98,86,1,'SALSA LINDA DE  1 KG','UNIDAD',1,1.00,50.00,50.00,25.00,75.00,53,1.00,1.00,1),(99,86,6,'ARROZ PINCO DE 1LB','UNIDAD',1,10.00,25.00,250.00,62.50,312.50,69,1.00,10.00,1),(100,87,1,'SALSA LINDA DE  1 KG','UNIDAD',1,10.00,50.00,500.00,90.00,590.00,53,1.00,10.00,1),(101,87,4,'SAZON RANCHERO','UNIDAD',1,3.00,10.00,30.00,5.40,35.40,68,1.00,3.00,1),(102,87,6,'ARROZ PINCO DE 1LB','SACO',5,3.00,7500.00,22500.00,4050.00,26550.00,70,300.00,900.00,1),(103,88,6,'ARROZ PINCO DE 1LB','SACO',5,1.00,7500.00,7500.00,1350.00,8850.00,70,300.00,300.00,1),(104,89,33,'ACEITE CRISOL UN LITRO','CAJA',3,2.00,600.00,1200.00,216.00,1416.00,52,10.00,20.00,1),(105,90,1,'SALSA LINDA DE  1 KG','UNIDAD',1,1.00,50.00,50.00,9.00,59.00,53,1.00,1.00,1),(106,90,4,'SAZON RANCHERO','UNIDAD',1,1.00,10.00,10.00,1.80,11.80,68,1.00,1.00,1),(107,90,6,'ARROZ PINCO DE 1LB','SACO',5,1.00,7500.00,7500.00,1350.00,8850.00,70,300.00,300.00,1),(108,91,2,'LECHE LISTA MIL ILT','CAJA',3,1.00,500.00,500.00,90.00,590.00,63,10.00,10.00,1),(109,92,2,'LECHE LISTA MIL ILT','UNIDAD',1,2.00,50.00,100.00,18.00,118.00,62,1.00,2.00,1),(110,93,5,'LECHE NIDO DE UN LT','CAJA',3,3.00,525.00,1575.00,283.50,1858.50,72,15.00,45.00,1),(111,94,7,'PLATANO VERDE','UNIDAD',1,50.00,15.00,750.00,135.00,885.00,75,1.00,50.00,1);
/*!40000 ALTER TABLE `detalle_recepcion_mercancia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_salida_inventario`
--

DROP TABLE IF EXISTS `detalle_salida_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_salida_inventario` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `salida_inventario` int NOT NULL,
  `articulo` int NOT NULL,
  `descripcion_articulo` varchar(200) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `unidad` int NOT NULL,
  `articulo_empaque` int DEFAULT NULL,
  `factor_conversion` decimal(12,2) DEFAULT NULL,
  `cantidad_fisica_base` decimal(12,2) DEFAULT NULL,
  `cantidad` decimal(12,2) NOT NULL,
  `sub_total` decimal(12,2) DEFAULT NULL,
  `itbis` decimal(12,2) DEFAULT NULL,
  `total` decimal(12,2) DEFAULT NULL,
  `existencia` decimal(12,2) DEFAULT NULL COMMENT 'Existencia despues de la salida',
  `precio_compra` decimal(12,2) DEFAULT NULL,
  `precio_venta` decimal(12,2) DEFAULT NULL,
  `costo_unitario` decimal(12,2) DEFAULT NULL,
  `existencia_anterior` decimal(12,2) DEFAULT NULL COMMENT 'Existencia antes de la salida',
  `cantidad_solicitada` decimal(12,2) DEFAULT NULL,
  `almacen` int DEFAULT NULL,
  `nombre_almacen` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_det_salida_idx` (`salida_inventario`),
  KEY `fk_det_articulo_idx` (`articulo`),
  KEY `fk_det_unidad_idx` (`unidad`),
  KEY `fk_det_almacen_idx` (`almacen`),
  KEY `fk_det_art_empq_idx` (`articulo_empaque`),
  CONSTRAINT `fk_det_almacen` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_det_art_empq` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_det_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `fk_det_salida` FOREIGN KEY (`salida_inventario`) REFERENCES `salida_inventario` (`codigo`),
  CONSTRAINT `fk_det_unidad` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=302 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_salida_inventario`
--

LOCK TABLES `detalle_salida_inventario` WRITE;
/*!40000 ALTER TABLE `detalle_salida_inventario` DISABLE KEYS */;
INSERT INTO `detalle_salida_inventario` VALUES (201,224,4,'SAZON RANCHERO',1,68,1.00,2.00,2.00,30.00,5.40,35.40,15.00,10.00,15.00,10.00,15.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(202,225,4,'SAZON RANCHERO',1,68,1.00,5.00,5.00,NULL,NULL,NULL,10.00,10.00,15.00,10.00,10.00,NULL,1,NULL),(203,226,4,'SAZON RANCHERO',1,68,1.00,3.00,3.00,45.00,8.10,53.10,13.00,10.00,15.00,10.00,13.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(204,227,4,'SAZON RANCHERO',1,68,1.00,4.00,4.00,60.00,10.80,70.80,22.00,10.00,15.00,10.00,22.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(205,228,2,'LECHE LISTA MIL ILT',1,62,1.00,3.00,3.00,210.00,37.80,247.80,152.00,80.00,70.00,80.00,152.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(206,229,4,'SAZON RANCHERO',1,68,1.00,3.00,3.00,45.00,8.10,53.10,28.00,10.00,15.00,10.00,28.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(207,229,2,'LECHE LISTA MIL ILT',1,62,1.00,2.00,2.00,140.00,25.20,165.20,152.00,80.00,70.00,80.00,152.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(208,229,33,'ACEITE CRISOL UN LITRO',3,52,10.00,10.00,1.00,800.00,144.00,944.00,286.00,60.00,800.00,60.00,286.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(209,230,2,'LECHE LISTA MIL ILT',3,63,10.00,10.00,1.00,700.00,126.00,826.00,152.00,80.00,700.00,80.00,152.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(210,230,2,'LECHE LISTA MIL ILT',2,64,5.00,5.00,1.00,300.00,54.00,354.00,152.00,80.00,300.00,80.00,152.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(211,231,33,'ACEITE CRISOL UN LITRO',3,52,10.00,10.00,1.00,800.00,144.00,944.00,286.00,60.00,800.00,60.00,286.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(212,231,4,'SAZON RANCHERO',1,68,1.00,1.00,1.00,15.00,2.70,17.70,28.00,10.00,15.00,10.00,28.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(213,231,34,'ARROZ  LA GARZA DE  1LBS',1,66,1.00,1.00,1.00,30.00,5.40,35.40,20.00,25.00,30.00,25.00,20.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(214,231,2,'LECHE LISTA MIL ILT',3,63,10.00,10.00,1.00,700.00,126.00,826.00,152.00,80.00,700.00,80.00,152.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(215,231,33,'ACEITE CRISOL UN LITRO',1,51,1.00,1.00,1.00,80.00,14.40,94.40,286.00,60.00,80.00,60.00,286.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(216,232,33,'ACEITE CRISOL UN LITRO',1,51,1.00,2.00,2.00,160.00,28.80,188.80,275.00,60.00,80.00,60.00,275.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(217,232,2,'LECHE LISTA MIL ILT',3,63,10.00,10.00,1.00,700.00,126.00,826.00,124.00,80.00,700.00,80.00,124.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(218,232,2,'LECHE LISTA MIL ILT',2,64,5.00,5.00,1.00,300.00,54.00,354.00,124.00,80.00,300.00,80.00,124.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(219,233,33,'ACEITE CRISOL UN LITRO',1,51,1.00,1.00,1.00,80.00,14.40,94.40,273.00,60.00,80.00,60.00,273.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(220,233,4,'SAZON RANCHERO',1,68,1.00,2.00,2.00,30.00,5.40,35.40,26.00,10.00,15.00,10.00,26.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(223,235,34,'ARROZ  LA GARZA DE  1LBS',7,67,10.00,10.00,1.00,NULL,NULL,NULL,39.00,25.00,30.00,25.00,39.00,NULL,1,NULL),(224,236,34,'ARROZ  LA GARZA DE  1LBS',7,67,10.00,10.00,1.00,NULL,NULL,NULL,17.00,25.00,30.00,25.00,17.00,NULL,1,NULL),(225,237,33,'ACEITE CRISOL UN LITRO',1,51,1.00,1.00,1.00,80.00,14.40,94.40,273.00,60.00,80.00,60.00,273.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(226,237,34,'ARROZ  LA GARZA DE  1LBS',1,66,1.00,3.00,3.00,90.00,16.20,106.20,29.00,25.00,30.00,25.00,29.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(227,238,33,'ACEITE CRISOL UN LITRO',3,52,10.00,10.00,1.00,800.00,144.00,944.00,273.00,60.00,800.00,60.00,273.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(228,239,33,'ACEITE CRISOL UN LITRO',3,52,10.00,10.00,1.00,NULL,NULL,NULL,283.00,60.00,80.00,60.00,283.00,NULL,1,NULL),(229,240,33,'ACEITE CRISOL UN LITRO',3,52,10.00,10.00,1.00,800.00,144.00,944.00,267.00,60.00,800.00,60.00,267.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(230,240,2,'LECHE LISTA MIL ILT',1,62,1.00,3.00,3.00,210.00,37.80,247.80,115.00,80.00,70.00,80.00,115.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(231,241,33,'ACEITE CRISOL UN LITRO',1,51,1.00,2.00,2.00,160.00,28.80,188.80,267.00,60.00,80.00,60.00,267.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(232,242,2,'LECHE LISTA MIL ILT',1,62,1.00,3.00,3.00,210.00,37.80,247.80,112.00,80.00,70.00,80.00,112.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(233,243,33,'ACEITE CRISOL UN LITRO',3,52,10.00,20.00,2.00,NULL,NULL,NULL,273.00,60.00,80.00,60.00,273.00,NULL,1,NULL),(234,244,2,'LECHE LISTA MIL ILT',1,62,1.00,4.00,4.00,280.00,50.40,330.40,112.00,80.00,70.00,80.00,112.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(235,244,33,'ACEITE CRISOL UN LITRO',1,51,1.00,3.00,3.00,240.00,43.20,283.20,267.00,60.00,80.00,60.00,267.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(236,245,33,'ACEITE CRISOL UN LITRO',1,51,1.00,2.00,2.00,160.00,28.80,188.80,263.00,60.00,80.00,60.00,263.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(237,245,2,'LECHE LISTA MIL ILT',1,62,1.00,2.00,2.00,140.00,25.20,165.20,108.00,80.00,70.00,80.00,108.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(238,245,4,'SAZON RANCHERO',1,68,1.00,2.00,2.00,30.00,5.40,35.40,24.00,10.00,15.00,10.00,24.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(239,246,33,'ACEITE CRISOL UN LITRO',1,51,1.00,2.00,2.00,160.00,28.80,188.80,281.00,60.00,80.00,60.00,281.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(240,247,34,'ARROZ  LA GARZA DE  1LBS',7,67,10.00,10.00,1.00,NULL,NULL,NULL,17.00,25.00,30.00,25.00,17.00,NULL,1,NULL),(241,248,5,'LECHE NIDO DE UN LT',3,72,15.00,15.00,1.00,NULL,NULL,NULL,45.00,35.00,45.00,35.00,45.00,NULL,1,NULL),(242,249,5,'LECHE NIDO DE UN LT',1,71,1.00,2.00,2.00,90.00,16.20,106.20,45.00,35.00,45.00,35.00,45.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(244,251,33,'ACEITE CRISOL UN LITRO',3,52,10.00,30.00,3.00,2400.00,432.00,2832.00,279.00,60.00,800.00,60.00,279.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(247,253,33,'ACEITE CRISOL ',3,52,10.00,10.00,1.00,NULL,NULL,NULL,273.00,60.00,80.00,60.00,273.00,NULL,1,NULL),(248,254,33,'ACEITE CRISOL ',1,51,1.00,1.00,1.00,80.00,14.40,94.40,249.00,60.00,80.00,60.00,249.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(249,254,33,'ACEITE CRISOL ',3,52,10.00,10.00,1.00,800.00,144.00,944.00,249.00,60.00,800.00,60.00,249.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(252,256,3,'SOPITA DOÃ‘A GALLINA',3,74,48.00,48.00,1.00,720.00,129.60,849.60,4.00,10.00,720.00,10.00,4.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(254,258,3,'SOPITA DOÃ‘A GALLINA',6,73,1.00,2.00,2.00,30.00,5.40,35.40,56.00,10.00,15.00,10.00,56.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(255,259,2,'LECHE LISTA MIL ILT',3,63,10.00,10.00,1.00,700.00,126.00,826.00,144.00,80.00,700.00,80.00,144.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(256,260,2,'LECHE RICA LISTA MIL ',3,63,10.00,10.00,1.00,NULL,NULL,NULL,128.00,80.00,90.00,80.00,128.00,NULL,1,NULL),(257,261,2,'LECHE LISTA MIL ILT',3,63,10.00,10.00,1.00,700.00,126.00,826.00,144.00,80.00,700.00,80.00,144.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(261,263,2,'LECHE RICA LISTA MIL ',3,63,10.00,30.00,3.00,NULL,NULL,NULL,118.00,80.00,90.00,80.00,118.00,NULL,1,NULL),(262,264,2,'LECHE RICA LISTA MIL ',3,63,10.00,10.00,1.00,700.00,126.00,826.00,124.00,80.00,700.00,80.00,124.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(263,265,2,'LECHE RICA LISTA MIL ',1,62,1.00,6.00,6.00,NULL,NULL,NULL,114.00,80.00,90.00,80.00,114.00,NULL,2,NULL),(264,266,2,'LECHE RICA LISTA MIL ',1,62,1.00,3.00,3.00,210.00,37.80,247.80,114.00,80.00,70.00,80.00,114.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(265,267,2,'LECHE RICA LISTA MIL ',1,62,1.00,1.00,1.00,70.00,12.60,82.60,105.00,80.00,70.00,80.00,105.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(266,268,2,'LECHE RICA LISTA MIL ',1,62,1.00,1.00,1.00,70.00,12.60,82.60,104.00,80.00,70.00,80.00,104.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(267,269,3,'SOPITA DOÃ‘A GALLINA',6,73,1.00,2.00,2.00,NULL,NULL,NULL,4.00,10.00,15.00,10.00,4.00,NULL,1,NULL),(268,270,2,'LECHE RICA LISTA MIL ',2,64,5.00,10.00,2.00,600.00,108.00,708.00,103.00,80.00,300.00,80.00,103.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(271,273,1,'SALSA LINDA DE  1 KG',1,53,1.00,1.00,1.00,60.00,10.80,70.80,28.00,50.00,60.00,50.00,28.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(275,275,2,'LECHE RICA LISTA MIL ',3,63,10.00,10.00,1.00,700.00,126.00,826.00,103.00,80.00,700.00,80.00,103.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(279,277,3,'SOPITA DOÃ‘A GALLINA',6,73,1.00,1.00,1.00,15.00,2.70,17.70,54.00,10.00,15.00,10.00,54.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(282,279,3,'SOPITA DOÃ‘A GALLINA',6,73,1.00,2.00,2.00,30.00,5.40,35.40,53.00,10.00,15.00,10.00,53.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(285,281,5,'LECHE NIDO DE UN LT',1,71,1.00,2.00,2.00,90.00,16.20,106.20,43.00,35.00,45.00,35.00,43.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(288,283,2,'LECHE RICA LISTA MIL ',2,64,5.00,5.00,1.00,300.00,54.00,354.00,93.00,80.00,300.00,80.00,93.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(293,287,3,'SOPITA DOÃ‘A GALLINA',3,74,48.00,48.00,1.00,720.00,129.60,849.60,51.00,10.00,720.00,10.00,51.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(294,288,2,'LECHE RICA LISTA MIL ',3,63,10.00,20.00,2.00,NULL,NULL,NULL,88.00,80.00,90.00,80.00,88.00,NULL,1,NULL),(295,289,5,'LECHE NIDO DE UN LT',1,71,1.00,2.00,2.00,90.00,16.20,106.20,41.00,35.00,45.00,35.00,41.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(296,290,2,'LECHE RICA LISTA MIL ',1,62,1.00,4.00,4.00,280.00,50.40,330.40,88.00,80.00,70.00,80.00,88.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(298,292,7,'PLATANO VERDE',1,75,1.00,50.00,50.00,NULL,NULL,NULL,50.00,12.00,20.00,12.00,50.00,NULL,1,NULL),(299,293,7,'PLATANO VERDE',1,75,1.00,6.00,6.00,120.00,21.60,141.60,50.00,12.00,20.00,12.00,50.00,NULL,2,'DISPONIBLE PARA LA VENTA'),(300,294,7,'PLATANO VERDE',1,75,1.00,5.00,5.00,NULL,NULL,NULL,44.00,12.00,20.00,12.00,44.00,NULL,2,NULL),(301,295,7,'PLATANO VERDE',1,75,1.00,2.00,2.00,NULL,NULL,NULL,39.00,12.00,20.00,12.00,39.00,NULL,2,NULL);
/*!40000 ALTER TABLE `detalle_salida_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_traslado_inventario`
--

DROP TABLE IF EXISTS `detalle_traslado_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_traslado_inventario` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `traslado` int NOT NULL,
  `articulo` int NOT NULL,
  `descripcion_articulo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `unidad` int DEFAULT NULL,
  `nombre_unidad` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cantidad_enviada` decimal(12,2) NOT NULL,
  `cantidad_recibida` decimal(12,2) DEFAULT '0.00',
  `costo_unitario` decimal(12,2) NOT NULL DEFAULT '0.00',
  `precio_compra` decimal(12,2) NOT NULL,
  `articulo_empaque` int DEFAULT NULL,
  `factor_conversion` decimal(12,2) DEFAULT NULL,
  `cantidad_fisica_base` decimal(12,2) DEFAULT NULL,
  `sub_total` decimal(12,2) DEFAULT NULL,
  `itbis` decimal(12,2) DEFAULT NULL,
  `total` decimal(12,2) DEFAULT NULL,
  `existencia_anterior` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_det_traslado_articulo` (`articulo`),
  KEY `fk_det_traslado_cabecera` (`traslado`),
  KEY `fk_det_traslado_unidad_idx` (`unidad`),
  KEY `fk_det_traslado_art_empq_idx` (`articulo_empaque`),
  CONSTRAINT `fk_det_traslado_art_empq` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_det_traslado_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_det_traslado_cabecera` FOREIGN KEY (`traslado`) REFERENCES `traslado_inventario` (`codigo`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_det_traslado_unidad` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_traslado_inventario`
--

LOCK TABLES `detalle_traslado_inventario` WRITE;
/*!40000 ALTER TABLE `detalle_traslado_inventario` DISABLE KEYS */;
INSERT INTO `detalle_traslado_inventario` VALUES (72,71,4,'SAZON RANCHERO',1,'UNIDAD',5.00,5.00,10.00,10.00,68,1.00,5.00,NULL,NULL,NULL,10.00),(73,72,34,'ARROZ  LA GARZA DE  1LBS',7,'FALDO',1.00,1.00,250.00,250.00,67,10.00,7.00,NULL,NULL,NULL,17.00),(74,73,33,'ACEITE CRISOL UN LITRO',3,'CAJA',1.00,1.00,600.00,600.00,52,10.00,273.00,NULL,NULL,NULL,283.00),(75,74,33,'ACEITE CRISOL UN LITRO',3,'CAJA',2.00,2.00,600.00,600.00,52,10.00,253.00,NULL,NULL,NULL,273.00),(76,75,34,'ARROZ  LA GARZA DE  1LBS',7,'FALDO',1.00,1.00,250.00,250.00,67,10.00,7.00,NULL,NULL,NULL,17.00),(77,76,5,'LECHE NIDO DE UN LT',3,'CAJA',1.00,1.00,525.00,525.00,72,15.00,30.00,NULL,NULL,NULL,45.00),(78,77,33,'ACEITE CRISOL ',3,'CAJA',1.00,1.00,600.00,600.00,52,10.00,263.00,NULL,NULL,NULL,273.00),(79,78,2,'LECHE RICA LISTA MIL ',3,'CAJA',1.00,1.00,500.00,500.00,63,10.00,118.00,NULL,NULL,NULL,128.00),(80,79,2,'LECHE RICA LISTA MIL ',3,'CAJA',3.00,3.00,500.00,500.00,63,10.00,88.00,NULL,NULL,NULL,118.00),(81,80,3,'SOPITA DOÃ‘A GALLINA',6,'SOBRE',2.00,2.00,10.00,10.00,73,1.00,2.00,NULL,NULL,NULL,4.00),(82,81,2,'LECHE RICA LISTA MIL ',3,'CAJA',2.00,2.00,500.00,500.00,63,10.00,68.00,NULL,NULL,NULL,88.00),(83,82,7,'PLATANO VERDE',1,'UNIDAD',50.00,50.00,12.00,12.00,75,1.00,0.00,NULL,NULL,NULL,50.00);
/*!40000 ALTER TABLE `detalle_traslado_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `documento`
--

DROP TABLE IF EXISTS `documento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `documento` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `numero_documento` int DEFAULT NULL,
  `tipo_documento` int DEFAULT NULL,
  `secuencia_documento` int DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT NULL,
  `fecha_actualizacion` timestamp NULL DEFAULT NULL,
  `comentario` text CHARACTER SET latin1 COLLATE latin1_swedish_ci,
  `anulada` bit(1) DEFAULT NULL,
  `fecha_anulada` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario` int DEFAULT NULL,
  `nombre_usuario` varchar(80) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `anulada_por` varchar(80) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_ent_inv_sec_doc` (`secuencia_documento`) USING BTREE,
  KEY `fk_ent_inv_tipo_doc` (`tipo_documento`) USING BTREE,
  KEY `fk_ent_inv_usuario` (`usuario`) USING BTREE,
  CONSTRAINT `fk_ent_inv_sec_doc_copy` FOREIGN KEY (`secuencia_documento`) REFERENCES `secuencia_documento` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_ent_inv_usuario_copy` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `documento`
--

LOCK TABLES `documento` WRITE;
/*!40000 ALTER TABLE `documento` DISABLE KEYS */;
/*!40000 ALTER TABLE `documento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `empresa`
--

DROP TABLE IF EXISTS `empresa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `empresa` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(120) NOT NULL,
  `diereccion` varchar(200) NOT NULL,
  `telefono` varchar(25) NOT NULL,
  `logo` blob,
  `rnc` varchar(15) NOT NULL,
  `fecha` datetime NOT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `empresa`
--

LOCK TABLES `empresa` WRITE;
/*!40000 ALTER TABLE `empresa` DISABLE KEYS */;
INSERT INTO `empresa` VALUES (1,'COLMADO LA SINPATIA','C/29  NO 10  LAS COLINAS SANTIAGO','809-5759786',_binary 'ÿ\Øÿ\à\0JFIF\0\0\0\0\0\0ÿ\Û\0„\0		\n\n	\r\r\r \"\" $(4,$&1\'-=-157:::#+?D?8C49:7\n\n\n\r\r\Z\Z7%%77777777777777777777777777777777777777777777777777ÿÀ\0\0\0þ\"\0ÿ\Ä\0\0\0\0\0\0\0\0\0\0\0\0\0\0ÿ\Ä\0H\0	\0\0\0!1AQaq\"‘¡2BRb±Á#r‚’\Ñ3¢²\Â\áð$C%6DUcs“”ñÿ\Ä\0\0\0\0\0\0\0\0\0\0\0\0\0ÿ\Ä\05\0\0\0\0\0\0\0!1AQað\"q¡±Á2‘\ÑBC#3b\árÿ\Ú\0\0\0?\0¼h¢ŠŠ(¢„\"Š(¡\\CM©\Çµ	Q\îV\Þe\Þõ•\ÈÅ‡CPRr„:‚\Ú\0m\ÂG?8\ì\ÍZ´T\ÌY]U,zÁk\ä¨W¡>‡–†˜’\ê¢¿gX\Ý\ãŒWT\ëø\Ø\\¦K¯û\Éa-•)(\ï^>¯€\çÏ•^Ÿ®u\\k,r\Æ\í\î¨pm\'\nY\î\Ï`\ï>”@l,Å´ƒ‰\ëìªG‡›]U®%Mº•6	À+F{¸\×Ê‘´@›ª\æý#xZ½…\n\Â\Z„«ð¤võs<¼¸\0:°\0\0(€g\ZR)\Ã\ÜX~¡¶\Û%]m¦\r \Þ÷XÔ“6—e\Âöˆ`—\îºÂ¸4÷ƒ\Ý\çQÇÁ\æ+®%\ÒtEÌ•‘÷w$úÒ»8”¶ñ~{°˜ƒ­(6\ä¹v,p(P=\Å&»\â\Ú\×:\Z‰\ÅöŽe\\\n‡aOô4\Ëg½µq=R\Ó\ÕHû™\ÈWˆ?¥J\ÖEN”š3€³‡;ü-Šm­\Æ‰§’I³\\µ\É(u·:…+6Rr“\Þ}:¥AI\nI$d\Ú+\í—WRÚ‡c\r±ßžÕ«GL\êv\à.¸\Ý\ÉQE(›EVº\Û(\Þóˆm=\ëP€	\È.egPÚ¹\Ç~ˆm £\Õu\áE=™Á\ã]‚\ín\'\Ü\ÇóŠ\Â\ì\Ê.V§\Â\Ò\á\Æ\ä(#Ž?OZr”¾žv½Àt¥Qlð=Œ ›$J²z;„mViw‰\ë\r2úB\Ò\Ø\ÚA;žO¦;\ê¸l¤-*Z\Ò%\n\Î\àjZV 2¶d\ÈR\Õ!m\'`\à„6\Ç	€\ã·\á^\Ò7›¯\'\Ú\Çb+K~“ž_x©\ÐHaŒð@\ï?ˆöŸJ‰­ñ#*SÁ¤º\ÃDý§\Ý\r§\âi\Â\Õ\Ñ\ã\Ò/L¸\Çörr¤\Æ\Ê÷Ž\à®óÁ®¹\æë’Jn3Y\èQ®KZž\Óñ±ƒ-Å©Y_pJ²	\ï\Æ\0«¦\Ð\Êm!(@ä”Œ\éXEŒ\Ì8\ÍÆŠ\ÚZe´\íB8[i\Ö3²ÕŠ<\r²(¢Šš±QEEQBEP„QEq\Ý\îq¬ö\×\ç\Í^\ÖYN\ãŽdö\ÞI\à(F\Õ­µ;\Zj\Ô]>ü·²˜\Í}\ã\ÚO€\È\Ïú\Õ9i‡/T\Þ\Öô\×´g|—sŒ\'±)\î\Ï!\Ý\Ä\×%þñ7PÝœ/*qÃµ¦S\Ä6Ÿ²„ÿ\0¾$š±´õ­6‹[Q°:\ãï¼¡Ú³\Ï\ÐrUƒ¥«-\Äß¨\ä9q=rZtñXYH2\Ûl¶\ÛM!(m°”$`$ÁH(\Þ\É)m)Ä­Ü’°žH\Ï3\ÚqùÕ‚9Š®\Ü^qj\É\Ü\â½\ã\ÚsÇd\è[—¼ß‡\ÊCM\Û¸ü)fmjº\ÚÛ“	”\Ù\ê\ÜðpG\Ìy\×\î2b\\\Ýe\ÂT‘\àGõ\Í2iF”Ý¤-CcŠPò\åúV:ŠÎ¹ÀI\ÆB´£\ï§\ÃÆ­Ž»Y\çº.:\ä=<2Pc£0w‰ëŸ¯ŠPB”…¥m¨¥i9J‡0iö\Ñ;\Û\à6ùÀ_\ÕXŠ\ï>´\ÜGW	ùa\'«ahBò9gú|\éÓ¢·‡´\Ü#¨v!Ô’9q þ•£WB+[{)]R\è%¶â¥‘Kƒ(Ž\éûMd¨2\Ò2¨\Îÿ\0.i¶¾UCô\ìV\Í\æþKk·;‚K ƒ‚#°\ÑN/0\Ó\é\ÚóiXüB•5M’\à¨\Ý]‘AÀz\ÂV@\î§4Œúf\ã®>\ÖVv\æ\á$Œ\Ò\Å\çP†T¸ð0§:x„ž\á\Þ~T°ó®>\áq÷\âþò\ÎMN@\Ó©\Ïù\å%¦“ö[9Qðð¬&X§½!Jb3M²=\Ö\ÐÙŸ\Ó\çL\ÓIENp1\Âû\Ï_,J˜\ëj7´ò|¨:Í‡œŽ\àr;‹ic\í!X55L\ÉS™š´2\Êx’•O\è<\ë‚\î–S$¦2™\êî¶–Õ¸ã¼žóN2¦)_«a¿	G\Ò\Ë5\á\År<\áu\Õ8 ¥£=§…‹\Ë°_XWV•’pNqù\Zg \rkž\ërVÖ‹aÄ—Ðµ¶¼”+iP\î\Ïe4G×—(5tx\Í\r¨e-¨\ày\î\ÏE^fFjT_bq—R…uù\n\n“¸ô}<¥§­\Êd¤cº\çöJ\í	V=\äùñó«kµ\\\Ø\æe\ì,¥,\Ý\"\Æ}ij\ï\ÙIÿ\0¬ƒ¹¿^\Ñó§„©+HRRH\È \äU¿£\éÎ°ûs\ËL:=\æm\Í\ã<ŠTœŽGÊ˜4M²ýe\n…q.	h¡\ÝÅ¥w\Ý?#\çL\Æ\ç\ìpM\Âùv<&\Ú(¢®M\"Š(¡¢Š(B(¢ŠŠ¦:X\Ô_I]…¢2É‹Ys–ö?\Ê<\É\î«;WÞ…ƒOK¸su	\Ú\Ê{\ÜQ\Â~g>B¼\î¥)J*qJZ\ÔIR”rTO2|j‰¹3NËœE0\è[x™yö…Œµ=g¾~¯\ê}*È¥\Ý	Ù¬)xŒ.J\Ô\áòhü³\ëLUá´œúÚ—p}¿\êÔŒY¨\é1ß¾\Ý\\\Âz¸\í«i pm ýQ\âyú\æœ\ëšm–\Ãm !°\n®–«³‡Žñ\Øx%\ê©{Ahqî£Šú\Ûhi´¶\ÚBP@\ì¤\ìð§®Ø òûÇ»Ê£Ò’¥¤eJ8Æ›\ã2˜ñ\Ûe<1\æ{M=¡¨…L\Å\ïÍ­õ+µR\êÙ…»\Ô6§³¢]†\æ\Ô(\éö—Àw.-8Ç®ŠP\è¹\nÉ¤…\'lmª`ƒ¼p><\rY•\ÊÅ¶${„‰\ì´\"JR—T>\Þ\Ü\àã¿?*ö%—pr\Â|7<n]TQEM^Š(¢„({\Ü\0¤™L§\n\Ú\Ú;\éj\á5›|e>ù\à8%#š\Ïp§\Þ`Œƒ\ÌU7¬$¥\Û\äˆì¨˜ñ”[@ñ\í?•\ç«ôKd¨lÉ§o\\\ÕÏ¯0@Fý\Ýr\\œ›Š\É}xo<\ZI÷Gõó5É‚Ž—ÿ\0¢¾SE¢\Ð\Ì\Ë<C#8\ë”\é\íXÏ «¦š*8\ÆVnÌ–40\ËY!\Î\îÛšZ-©-!\Å%yÛŸµŽ\ß*–°BE\Æ\ÎœÍ¥ ý\ÓÄƒ\è@®}B n\Ï! %\r¶„Ž\0\09‰©­\'\ì\êiÕ¶\n÷R\êsÀ\ã8P\î\Ï\éK\ÖL\îÇ¬c\á˜!3Cmhmö_\Ï\"OD7å ¿§&®4T\äpO\Ç\ßG¡\ã\ê{ªÏªR-\ë±M\Ê!!{‘%±\Ë\'’“\äp¬þõ^–\éŒ\Ü`G›[™\Ú\\Að#5µG0š¸oJ¢<.ºè¢Š)´º(¢ŠŠ(¢„\"Š(¡¢Š(B©úhº\ï™\Ð\Úý\Ö\Ðdºy\ÊSò\nø\Õh¬\à\íW`ñ©\íu8\Ü5u\Ñ\ì\å(x²${¿˜\'Ö¢\íu÷8l\ï¡8þ!H\Êû\ã¹h\Æ\Û4mÂŽ\"Cb2y4\ÚQð­\ÔfŠùñ$›”ú(¢Šº\í(\\Xp«\à)¦–l§&³\Ú>T\Í^\Ãôø™\Çý¾Ë­ÿ\0 ðEQ[©4QE!QEA8<‡\Z¡\ã\îŸ=½\ÜU!\àU\ã¸\äþf¯‚7;\Æ* \ÓvY	\Ô\Ç\ÐP\ËEA½ÃŠ\Ô=Ó\0{{\é!(Š\"ó¸ø—’M+Y,mº}oMwÚLfœ)…\Ì\ËÆ›”¡!(HJ@À\0`_h¯SW%C~\å¿MI3Hfô™ªc)›™{\ã\é\n\Äp#òøÔ†’Ž\ã]sª\0´\êPP°r<;LÜ µqŒX{#ŽR¡\Í\'¼Tfœ‹.\Ò\ãI`)R\â•g ‘ð)óV% 1\ß1o1q±g\nCx’\Ý\Ó#cµDô-À”©m\ç‚?#M]=ªÀý¹j%p]÷r~\ÂòG\Ïpô¨=|\Ð^Ÿ+\ímô+ó­GtE4\ÆÕ†7Ù™H>*O¼>[«cA\Ézp8>~SµM¸*ì¢Š+}f\"Š(¡¥\ím©F˜µ¢PŽu\×:¶Ò¥mH8\'$÷`S\rsKƒc‘Ü”\Â]Tw:Æ·}•`Œü	®:ö\É[˜×ƒ ¸\àªôt“¨•‡iŽ¦\Ï\"w\×d^•Ôž\ìø#,¿\Ç>Jó«\n\í9»]®T\çq²;JYøª­\è\âW\×u¿\ß\Ë*Œ\Ø\êÖ·À(Þ²³Ç‡jqû\ÕC±4u³¥š\'\È\èl¶rN\ä\Ê\ÏJ6%£.±=¥}\Õ2\ä£LöK\Ã7›;W6q¶]\n!.¸m$qÇ•A/Kè«Œ#rDX¦\"RV^Žú›@™;Tq©X\ÏÀ•§^oO8\Ë\Ì6\Â\Úe\Ô\'‚|.ucK¯™HTŠb\Ï\ì±À\ß;\ì—ž\ÚvI\áÖ­Nþ#“ù\×tm9¹\Ç+µÚ¦;¸{Ž%;\æ¢ÀÕ™¦´]ŸI\Ûº\ê‡Yr[i—8¶\É\Ç\ÕJ~Ò¼qž\à+š\ëÒªƒŠM¦Üž¨rzRðOðŽ^¦©h!\ÎV\Ç\Õ¶\ãŽ\ä°\ÂúF°±™\ÖW\çF@ûiK«Í²Uñ¾3\Òs\0*\ÐúV	\n\r:•`÷q\Ñ¤Kó(nv¹8S\í2\ã`y”Ÿˆó­=\'i\Ën£\Óñe‘)ö–›\ëR\í¬>òxøð\"¢t~Žª~ +eL\Ñ\Õ\Òñ\Ë\î \Òt{–¹„þ% ~¦¹\é9\î>\Ïhlw$ù&(¦ú{G7ø\ï\æ)3[9Þœ¿þ—~C¡\Ø\í@d¤\å?²R±ñUm=+\ê\âAö¨ƒÀEü\é^\Åg¹µnµ´\æO„¥#š”{\0ÿ\0|M?»Ð­\á1\ÊÚ»Áq\ìd¦–”“Ý»øiØ©©i†4SŸ$™“u\Ã¦\rNÒÕ¶B{‹\nAø…SE›¦{{\ÊJ/V\Ç\â\Í\Ø\ê\ë<Hà¯€5R^m3\ìw ]#©‰\ã´ñ\nŠI\í5\ÅW˜cpØ¡ˆ…\ê\ËE\â\Ûz\í6©¬Jk‘-,“\ÜG0|\rwW“­·	¶©‰™l”\ìY)\ä\ãJÁò=„xph\î–\â\ÊKq5:SG\0& ~\É~*còò¥¤§-Í¹©‡‚­\Z+]mö\ë.!\Æ\Ö2•¡@…ðG:Ê–SE,ÞK“Ô”ck`#‡,ó?N\ÜeˆqŠøo<;\Í*I%G$ñ\'¾¼\ß\ê\n¡„S»O\Â~Š3ròŠ(¢¼²\ÑE\ß\Ôü¶úÀR\Ûg‘Wme*\Í%„§k©öóøSƒG\Õõ¡‡^j­ta\ØošT\Ö)Ý¦¦ø‚\ÓI:-\ïg\Õ\ÖwI\à%%?Í”ÿ\0šžuiÆ›ŸûƒüB«{:Š/V\Õ\'šf2Gÿ\0\"ksAŸ\ìý|\\\Â\ë\ÒôQEzuŠ(¢„\"Š(¡	¥Ûˆ§Z„1\à\î#\Þ?0Ÿ*jcô.„²\ÙÇºü²fIH\æ{@>¥?\Ë]ý!ƒz×–\Ë8ú‰KhWñ«+þ\êE\Z\Ñ\r»\Òm­‰«CQR\Û\'.(%!	+Q\ç\Ã¥žnIò^’¢8\âi\æóñ\×%Õ«1¦:<ƒeB‚dJ\Â^\ï#\ë¸~$ZŸ\Ðñ˜Óš%¹s”\ZA–ú\È\ä?—h¤~§®ÚŠ\Ôì ¦¡\Î\ÃQ’\à)\Ã{\ÂJŽygw€Ûž5\'\Ò\Õ\çaŒ BC\Ò\0þ\â~Dÿ\0-€$ðP0:h\ã†ù¼—8õ\ÕÒ–«Ô’µÁRd7¼õgƒi\ï?ˆöŸJ|\Ð\Z¨\Ì7u½0)`)˜\î†G0Hûß—+tma‹\ï´HFè°\âÁ³õSòÏ ï«¨$\àd\Ñ1wœ)T h¥‡.?\ÊZ\ékP´„ô¹\Ò\Ô6žò®!“\éP]œ\è˜xå€§ñ»–6üóJ!jA»¢(®L¥¿õTy¯Ç¸xyÕ¥\íLÙ´„xw´+”ÁNU•,ðÀ\Î=*mv).7%ç‡³\Ð5Žúœonº\Íy²\×e»Ïˆ‡aZ§\Èohý£Q–¤ž\à`\×;\ì=\âÌ¦a\ÑÍ·›(Pô<jôºt§3Å«U¿¯Ž\ßµ\×:¤‘øF<Ws‚\Ë\ÒvžË‘ú™¬Œ!K\0¹d{ªJ»R~|A§›Z\×:\Ë6]QzÇ¶Á/ô#ln\Ýa¹jI„%/e\r¨ª\Óy\ÜGš³ü¢£-Z§QÜµ;Á\àe\É\Âc¹\ï6„œmðOh\ãÃ4\ërÞ˜\Ð0l1U…8„G\È\íJFV}yCôEjnònn\'(ˆŽ­³ø\×\Ï\àŸñRs¼\É-‚Ô ‰\Ñ\É<‚û…ú\ã\ì³\é\Ò™\Â\Ó\ì\Éržk¬SŽ±N \ã wŒúU8\Ë/>øa†]uòp\Zm²¥“Ý´q¯@j>‘š²\ß^€˜>\Ð\Ìp\Î%\Ü(+ `\à\ÛR·«ÍŸO²n\æ7	Œ…4\Ð:”ŒûÇ±)‰<3SZ\ÛpYo Ÿºpý[œ\ç\Ú.v\æÃ—lØ­“€·\ã­	\ÏvH\ÅkƒoqYE¾™jO\Ö\ÙRöù\àpõ¯@imcWH“iŸlB\ÙR¶)]bF@ ‚x­wOi\Ñ1[´X\í©s¨\ÂKm{4H\Î¬¬Ž8\â{MO¶·\r\Ñý:}v§{¯%LÚ¯š—GJ\ê£;2ú\Æ$¦”WŽ\Å˜Áñ§\ëGM¬¥›•…o<GÌ•Ÿ\ÜW™¦¿ù“t|¦žŒ#\ÈB”\ÛkW¿\Ô<\nV“\Ú=\ážY­–t\×F6¶\Ûm¢ô÷“\Å{A}ò9’~\Ê~B¸\é\âs1«³	u@w¸(K–µ’\é$iD7pBL\0\î\ë\äMJ·g1mŠño/«knÊµ²®\â®\Ê\î…Ò¼u\Ë(›mSLñ÷šwz‡vF…Ii-xGrú:E°°µ¡KIKbp>÷ŠÀ—E\ÐÍˆ\çˆ\ï¹Ú´\Í=t-»›ðØ¾VöbHyH	eÍª n\Øp<sS2ÛµØ˜~\ã9im”‚±¾	§<©2OK-\"H\íJSÀ.<µyó¬¸t\r³ö\ä‘™§ÿ\0/\Ïb\é2úõ’\×\ÝmyLH|ñ[g\nCi\î>\'\ã_:+¹\Þ.q§9r–¹šZPÒœpV2¡žÑ‚ž}õ_k;\êo×·g -,%´¡¤¯šROü“W´‹6˜‰\à_Xþ~ú¸«\áœzW£fo\î\ì\nÊ¨™MB\Ö8w\Ý÷\â	KY0ü\Ë,ñ+\ï\ïZv¥–ÊŽ7ƒ\Ø;ªµ¶\Çy›ý¹‰,ºÃ¾\Ø\È(u²…\Ú\'°ñ«Fñ\Ò|o)‹T3-¶\Î\Å}[g÷xG\n”±\Ý\ì\Úí„‰\ÂeAu\Ü\â¦\ÔR´(vd`üÁ\â¥`i`u\É7Ø—–:†3XöY½mM\ÔQEj,”QE!QEPoik{º•@K\âcCê…Š÷JrF;a0\ë½·®im\å½ \â¶Q\\\07J÷\ÛÙðX:\ÓO\'kÍ¡i8RA¯=\êÊ¹_®´½¿º\Ôü€¯B?ž¥Í¼öœ|+Í¶][l¹§¯N*‰÷-\Ý\Ñw¸\î·^Š\ï\è\â\Ø-ºR*Šp\ì±\í.dq÷‡\èœ\n]\é*~¦\Ü\í½ˆKM©ÁŽº2\âqJˆú£\Ã{û*\Æi´´\Ò@\ÂP<gV–wp¬\ÆU\á¨3¹¡\×\âªn4l‰3ZºÝ£­˜\Ì+s-:’•8±Èy$sñ8\ì\ç\ß\Ò\íñIC8\ëpu²€<v\ç\ÝIó ŸAMZ\ÃTF\Ópwš\è\"<q\ÍG¼þþ•K]c\Üxq«š”n2Ap«˜S€v7Ž\Ìb©}˜\Ü!lQ‡\ÖT\n™²`\ë‡)\ÃI[²\éIº²lTH| ûn¤\'\ê…y’~f²\èn9¹¸B@\\p¥„\'	¯#°s\áNú”À³\è\É,\Éi+ˆ\ÔP\Â\'\Î6¥?R‡G\Zbk¶s?\éI0›˜p¤0\ÚB\ÝBx¼‚S“»—f\rw\0] MM4\Ë°ð\á\ä¢:U¹	º›Ù [‚\ÐG\Éj÷•þ_…=hX\ÍX´K2_÷z\Æ\Õ1\âx‘ý\ÐTBE\ÇR’CR\'’\n‰!x$“Ä¹9«¥{\ÂbZÚ±\Å8S\à)\í¿a xR>\0\ÔX\ì\ËÊºª²\Z6o\ÌøŠ®¡¾‰÷\ÄÌ¹…->d>„ñRø\î\Øi\'	Æžµ“oZôÜ‰—\"•^o+KN\à\ä2\Ð÷º¤~y9®Šl(Ÿrr\í$nj¶´ž÷H\Î}øŸ\nù\Òô\î¾ÿ\0\Z\'lV7Ù¹gú$|k€Y„•t¯\×6\ìn\ß{{_Ë‚\á\Ð\n’\Óò…»o\ÒRÀJ§\ë8\ê¼\0ÙŽò@¬úEK\é0lPÊ‹q\Z/<\â\ÎT\ë«\æ¥\Õ`g?Š:/°¢\ÛeM\ÉÔƒ*zBó÷[û#\ç“\ç\áU•õ\Çoº®gVNùSK-ž\á»b~@\Z³5\Èdl\Õ\Ïpú[\ï³\Ó?}\ê\Ñ\Ð-5cÐÍ•\î%h\\\ÇIû§ˆþ\èMVl&n²\ÕI¨¥\éŽ\åGŸR\Ð\ç$òñó§Î”gû\r‰›$Ð•<\Øa$$g\ÍE#\ãPý7‰{¤·\Ú\"²„\ïYÀBNJ÷EIÙ¸3‚¢™ÅKYnó¶}òõö])3\×`´Ú¡²„mt©´ŽiJRA9\ç’H\ã\Û[:µ\íbu\Ù\Äû\Î+\Ù\Ú\'\îŒ\ê@þ\ZG\Õ\×\Õß¯/\ÏPRXH\Ù±±œ3\Ï\×•j0â´¶‡‚\Ã\r…\Üm\r0\Ðÿ\0©!|2Ið†^]¸.T2Hh›úžs÷?H}&\ßº_—¢£	Ø”\'\í»xøý\Ñ\ë\ßMð\ìq4®‚žì¶©n\ÄR¤¬€IQJ€$wñ\íª\ïEE-Wlm\â\\I®YQ\ÉQH+\ãß’3t¥©ç…ž\n·ÇŒ°e8ž ¹öQŸ\æ<\rE®È¼«¦…\Øâ£\éž¹ŸT±¢­ŸJ\ê[|E§{I_Zö~\êxùœZ°zY½ª±›Tu\írnK§ÿ\0Hs¤\ä\rFôA\r\r¦\évyA(@…+’@”~iøR†´¹Hº^Ý¸<…%4•\ÅB¹õ<v^*þ*/†?\'7´\éþ–^½“oEºa‰m®÷pe. ,¢*22¼¼w\ç€ò5·I¡¶ºS¼·\r!’Û™JF[\Ï÷·S£>Í¥ôªž\ë0b\ØûDÌŸ™¥N‰ ¼\è¹ß¥}y®”£\Ç\Þ*Yþc\á«lZÕž\ê‡J\Ù\æq\îý#\ïø\ÍXtQE^±EP„QE!QEEQº\çO\È\Ó÷·m²!>ñz;¸\ÊR¢wl=\Ä]\ãÖ¯*\Õ&;˜[™m\æV0¦\ÜHRT<A¨H\Ìa;CX\êY1Z\à\í	j×¯¬!6\ì™\Í\Äh\ë{ ¥] p÷‡ˆ¨›\ïI‘Sÿ\0-§c¹6R\Î\Ô8¦\Ô‚q¹gÃ‡L» 4ËŽoú7g\áC«J~\0\â¥mvM “n·\Çad`­(š¹\Ôm!\Ê\ê\ÝeN&µÇ‘µ¿)CH\èù¯\Ü\â\rV¥;4¨-¦ ”‘\ÉJ\ÇŽÄŽŸ>Zõþ’¸É¼3|²4u%\Ö7\0­\È9\n\ç\È\åV-\Ý[me¤fkr\ám\Ö\à«\év«þµ™_!ýiŽ­ÊŽq\åyŽ\\8g³>\Ç\æZm†P\Ë(JBBR”Œ\0!Y\×\Þ\é\n\Íoz}\ÎBXŒ\ÐÊ–¯™\'°&¤\ÖXóK\ÍP\é@m¬Ñ°VÚ´\ÅöÃ«S0\Úš\Ë.¸¦”Ëˆy\n	$“Ã˜\Ïwu#¬4•\êUµ‰\r6&\Ü§\æ†\Ô=\Ü!\Ï4¤d|ñÆ /\Ý1]e\Êöm3¶R¥mmo6]y\ÃÙµ\0\àyq¬Eç¥•¤-1%a\\RvRO’NÊ§\Ø\È&É£¥¥26K_Uet}jzÑ¦Xb[%™+Z\Üu\n\ÆA\'†q\à%\ë\Ý#zŸª$K‡oÆ—\Õ\á\Ä(~\Ï		 ‚|3\ëIÒºF\Ö\Ð\ä.4¹«aöþ³N\ÄBT=\nkºK\Öo:†™¸]YÚ†ÛŠ…)G¸\02jfŒ–uL:JH§tÀfo\ê½”GŒ\Ó\rŒ!´$w\01Tý¿Kß¬º¥‰K´È—4‚\æöTƒ\Ö\'Ž\ÉxŽu\Âö \éQˆ\æC\Ñf¥2U\ìH<<€\ÏÊ¡J\Z¸òº\'ÿ\0®\ßô¡Ô…ö\Ïb5{©Ã€Å¶\ê\äcM9t·\\\×~\Â\']\0\n;½•	þ\Í	=»O{I4¯o\è¾h–Q>\äØ‚T‰ŽTð \Èyñ\Çe&Ûµö¼ºIökl‡e?Œ\ìf\"@\ï<8R’µJ¶ÆŒ™\ä2IˆÛ€Ž5\ÃGÄ…(ôœñ‡\é\à¦5¶Œ¹›\à‘i\×À(m)m’‘Õ„€¸$v;\Û!L¸Ý…\æ\í\ÆK)(ƒ	J\n-õœV8o#†p<\Í#i.˜Ú’\ëqµ4v£\ïÀL\Øù\êÿ\0‰\'%#\Ä<ª\ÙJ’´…!AIP\È \äUº\r[³]’¾Ick\r²¾û*\Æ\ãÑ”\Ô\ÝVýž\à\ÓÔ²´T•³žÀG>}\ã…u\ê=¦4£¬mõòYx¨€·\Î\Ò	\ãÃ†x\ê±(¨j›š³úM\ÚI½½|Uo¥lg¬H±O‚\ìJ}OMukŸNx6§ \âqÃ‡mm\é#GL¹<\Í\Ê\Ê\Ð[­´\Zq„\àv”\ç‡òò«Š5B\Ö\\\ZFQ6µ \r¹n\Ïj¯gE\Ô\Ú\ÓÙ¡\\-\Æ\Íml…\ÉQt)o(v$vv\ã>Ž1Op¢1#1\"6a”!°\n\ßEI­¶j‰ª€0\0\Z7QE$º(¢ŠŠ(¢„\"Š(¡¢Š(B(¨÷þ•\Éê½›¹ù\×¿Nv“ük>jý_ñ<ø{a\Åû‡\ÝOQJ\Ë7o¶%zúV´›Ž}\ßl\ÏñR.Ó–6Ô¹Z)?\Ø&\Úó\çL\Z‘\ÛÆ¦r\Ü\Ú\Ô \ÛUÕ¥‹wòý3´yú¾­ý±·\íY\ëq\Ç<ü3^T»•›\ÍÈ»õý±ý\Ù\ï\ëšô”DH\íl·óH\Ê0\ä®\î‰ô\ë´{wH©k\éi\í-I’´\î\ê\ÆHBGp\äHÏ¥Szž\Ùz¶]\Õÿ\0!ñ=J\Ü$:¢®°ƒõ¾\Ñ\ÛÃ—p«¢}C2Ë§$½{(gN2¼G”\á;ƒŠWÐ	X\ÎO\Ï>\ËCþ\Ê\Ôv¯ü-\Æ\Þøðq\nÿ\0Zž±Ñ¼“š\å®;®nEzn\ëvV\ë±XCo\ïº\ØÝž\Ò	>x\ï§n´\äM¥vš\Ò~r1“%\Ò=\ä \'plw\09÷œšZ\éwHHB\ã\ß§­qú¶ˆ¤€\"52Œp\Ùßž<¸‘\Ê\Å\×_÷+Pcÿ\0-‘\Ëÿ\0mU\Ç:\í\0o+ f©=¬®\É\×QgM˜ò\Ñq’\Z’Ò–J0³„€9\r¤ŒxN3\è\æÜŒ5©„¦Xq-JBF»ŽÔ«÷‚ˆöƒ\à*¦´\îúb\Ý\ÕýolcoŸXšôH‹Kzø\áVÂˆ‹RÜ¡õ~x«%\îH\Ò\Õ\æ3\\¢] \ç*\Ú\ÚzØ‘ò\Ü#‹\ÏýewñÇ\áUCW©l\ë5\Å~KŽ·rmeÒµT\êF\à£ã€¡\ëN1u\îÕšVU¶ù=«T¹1”\ÃÁ\ãµ!Dcrxž8\çJý[,Ö­a+»Æ»\Ü\\R\Ò\Â-ùS,\rª%Å¬\ã\'#–{y\Ô\Z,\×\×w‹-5ihö\ÙQ\ï–öR\ÓR\Ü-\ÊB\0	\ã!xñÁÏˆ\æ¦zÔŽÉ\'OKYQŠžº!=\ç\nG$û\Ø\ì©n›T¡–•½R\Ù\Ù\ç»\'åš®º+\ZùƒŠöÿ\0ðýqRo~÷.lrô5ƒ\Ûú•õX\ë6¹\ï\ì¥wM\Ïw\í=¬%~•‰]_\Ù-\Ü.¿\Ü0\ë/“]ªƒuû_¨?­u7ô\ça_ñm¥c\Ó8ÿ\0…þB\ê\ÃKo\Ü\Ï\Ó7û6?J‘o~\Äõ›w\ãŽ\ÞU§ú\ß\ÚGˆ²]\ìÃ¼•QW¨\"Š(¡ÿ\Ù','100000','2026-06-19 00:00:00');
/*!40000 ALTER TABLE `empresa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `entrada_inventario`
--

DROP TABLE IF EXISTS `entrada_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `entrada_inventario` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `numero_documento` varchar(20) DEFAULT NULL,
  `tipo_documento` int DEFAULT NULL,
  `secuencia_documento` int DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT NULL,
  `comentario` text CHARACTER SET latin1 COLLATE latin1_swedish_ci,
  `fecha_contabilizacion` timestamp NULL DEFAULT NULL,
  `moneda` int DEFAULT NULL,
  `nombre_moneda` varchar(100) DEFAULT NULL,
  `anulada` bit(1) DEFAULT NULL,
  `fecha_anulada` timestamp NULL DEFAULT NULL,
  `usuario` int DEFAULT NULL,
  `nombre_usuario` varchar(80) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `anulada_por` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_ent_inv_usuario` (`usuario`),
  KEY `fk_ent_inv_sec_doc` (`secuencia_documento`),
  KEY `fk_ent_inv_tipo_doc` (`tipo_documento`),
  CONSTRAINT `fk_ent_inv_sec_doc` FOREIGN KEY (`secuencia_documento`) REFERENCES `secuencia_documento` (`codigo`),
  CONSTRAINT `fk_ent_inv_usuario` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=325 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `entrada_inventario`
--

LOCK TABLES `entrada_inventario` WRITE;
/*!40000 ALTER TABLE `entrada_inventario` DISABLE KEYS */;
INSERT INTO `entrada_inventario` VALUES (262,'AN-142',6,NULL,'2026-10-14','2026-10-14 14:01:46','2026-10-14 14:01:46','Entrada por anulacion de venta #142. ','2026-10-14 14:01:46',1,'DOP',_binary '\0',NULL,NULL,'Administrador',NULL),(263,'REC-56',9,NULL,'2026-10-14','2026-10-14 14:03:46','2026-10-14 14:03:46','Entrada por Recepcion de Mercancia #56. entrada de mercancia','2026-10-14 14:03:46',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(264,'AJ-71',3,NULL,'2026-10-14','2026-10-14 14:04:45','2026-10-14 14:04:45','Entrada por Traslado de Mercancia #71. ','2026-10-14 14:04:45',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(265,'FACT-143',7,NULL,'2026-10-14','2026-10-14 14:07:30','2026-10-14 14:07:30','DevoluciÃ³n Parcial de Factura #143. Motivo: devolucion parcial','2026-10-14 14:07:30',1,'DOP',_binary '\0',NULL,NULL,'ADMIN',NULL),(266,'FACT-144',7,NULL,'2026-10-14','2026-10-14 14:11:01','2026-10-14 14:11:01','DevoluciÃ³n Parcial de Factura #144. Motivo: dev parcial','2026-10-14 14:11:01',1,'DOP',_binary '\0',NULL,NULL,'ADMIN',NULL),(267,'AN-144',6,NULL,'2026-10-14','2026-10-14 14:11:34','2026-10-14 14:11:34','Entrada por anulacion de venta #144. ','2026-10-14 14:11:34',1,'DOP',_binary '\0',NULL,NULL,'Administrador',NULL),(268,'FACT-146',7,NULL,'2026-10-14','2026-10-14 14:23:19','2026-10-14 14:23:19','DevoluciÃ³n Parcial de Factura #146. Motivo: devolcucion del aceite','2026-10-14 14:23:19',1,'DOP',_binary '\0',NULL,NULL,'ADMIN',NULL),(269,'FACT-149',7,NULL,'2026-10-14','2026-10-14 14:47:22','2026-10-14 14:47:22','DevoluciÃ³n Parcial de Factura #149. Motivo: sfsfsd','2026-10-14 14:47:22',1,'DOP',_binary '\0',NULL,NULL,'ADMIN',NULL),(270,'FACT-150',7,NULL,'2026-10-14','2026-10-14 14:49:22','2026-10-14 14:49:22','DevoluciÃ³n Parcial de Factura #150. Motivo: devolucion del aceite','2026-10-14 14:49:22',1,'DOP',_binary '\0',NULL,NULL,'ADMIN',NULL),(271,'AJ-86',3,NULL,'2026-10-14','2026-10-14 15:35:15','2026-10-14 15:35:15','Entrada por Ajuste de Inventario #86. incremento ','2026-10-14 15:35:15',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(272,'AJ-72',3,NULL,'2026-10-14','2026-10-14 15:38:45','2026-10-14 15:38:45','Entrada por Traslado de Mercancia #72. ','2026-10-14 15:38:45',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(273,'AN-152',6,NULL,'2026-10-14','2026-10-14 15:49:35','2026-10-14 15:49:35','Entrada por anulacion de venta #152. ','2026-10-14 15:49:35',1,'DOP',_binary '\0',NULL,NULL,'Administrador',NULL),(274,'AJ-88',3,NULL,'2026-10-14','2026-10-14 17:36:10','2026-10-14 17:36:10','Entrada por Ajuste de Inventario #88. aumento 4','2026-10-14 17:36:10',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(275,'AJ-73',3,NULL,'2026-10-14','2026-10-14 17:36:48','2026-10-14 17:36:48','Entrada por Traslado de Mercancia #73. ','2026-10-14 17:36:48',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(276,'FACT-154',7,NULL,'2026-10-14','2026-10-14 17:38:50','2026-10-14 17:38:50','DevoluciÃ³n Parcial de Factura #154. Motivo: devolucion de la caja de aceite','2026-10-14 17:38:50',1,'DOP',_binary '\0',NULL,NULL,'ADMIN',NULL),(277,'FACT-154',7,NULL,'2026-10-14','2026-10-14 17:54:55','2026-10-14 17:54:55','DevoluciÃ³n Parcial de Factura #154. Motivo: dev una','2026-10-14 17:54:55',1,'DOP',_binary '\0',NULL,NULL,'ADMIN',NULL),(278,'FACT-156',7,NULL,'2026-10-14','2026-10-14 17:59:06','2026-10-14 17:59:06','DevoluciÃ³n Parcial de Factura #156. Motivo: una','2026-10-14 17:59:06',1,'DOP',_binary '\0',NULL,NULL,'ADMIN',NULL),(279,'AJ-74',3,NULL,'2026-10-14','2026-10-14 18:01:01','2026-10-14 18:01:01','Entrada por Traslado de Mercancia #74. ','2026-10-14 18:01:01',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(280,'FACT-157',7,NULL,'2026-10-14','2026-10-14 18:02:54','2026-10-14 18:02:54','DevoluciÃ³n Parcial de Factura #157. Motivo: dev','2026-10-14 18:02:54',1,'DOP',_binary '\0',NULL,NULL,'ADMIN',NULL),(281,'FACT-158',7,NULL,'2026-10-14','2026-10-14 18:08:42','2026-10-14 18:08:42','DevoluciÃ³n Parcial de Factura #158. Motivo: devolucioon de la leche','2026-10-14 18:08:42',1,'DOP',_binary '\0',NULL,NULL,'ADMIN',NULL),(282,'REC-59',9,NULL,'2026-10-14','2026-10-14 20:30:50','2026-10-14 20:30:50','Entrada por Recepcion de Mercancia #59. dsadasdasd','2026-10-14 20:30:50',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(283,'REC-60',9,NULL,'2026-10-14','2026-10-14 20:31:34','2026-10-14 20:31:34','Entrada por Recepcion de Mercancia #60. erwerwerwerewr','2026-10-14 20:31:34',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(284,'REC-62',9,NULL,'2026-10-14','2026-10-14 20:42:50','2026-10-14 20:42:50','Entrada por Recepcion de Mercancia #62. eerwerwerwewerwerwer','2026-10-14 20:42:50',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(285,'REC-63',9,NULL,'2026-10-14','2026-10-14 21:09:59','2026-10-14 21:09:59','Entrada por Recepcion de Mercancia #63. PROBANDO OC','2026-10-14 21:09:59',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(286,'REC-66',9,NULL,'2026-10-14','2026-10-14 21:28:43','2026-10-14 21:28:43','Entrada por Recepcion de Mercancia #66. wrwerwrew','2026-10-14 21:28:43',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(287,'REC-67',9,NULL,'2026-10-14','2026-10-14 21:29:06','2026-10-14 21:29:06','Entrada por Recepcion de Mercancia #67. werwerew','2026-10-14 21:29:06',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(303,'REC-83',9,NULL,'2026-10-15','2026-10-15 13:52:11','2026-10-15 13:52:11','Entrada por Recepcion de Mercancia #83. werwerwe','2026-10-15 13:52:11',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(304,'REC-84',9,NULL,'2026-10-15','2026-10-15 13:57:12','2026-10-15 13:57:12','Entrada por Recepcion de Mercancia #84. werwrwerwe','2026-10-15 13:57:12',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(305,'REC-86',9,NULL,'2026-10-15','2026-10-15 14:01:08','2026-10-15 14:01:08','Entrada por Recepcion de Mercancia #86. wrwrwer','2026-10-15 14:01:08',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(306,'REC-87',9,NULL,'2026-10-15','2026-10-15 14:13:34','2026-10-15 14:13:34','Entrada por Recepcion de Mercancia #87. rewrwerwer','2026-10-15 14:13:34',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(307,'REC-88',9,NULL,'2026-10-15','2026-10-15 14:13:50','2026-10-15 14:13:50','Entrada por Recepcion de Mercancia #88. dwqeqweqwew','2026-10-15 14:13:50',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(308,'REC-89',9,NULL,'2026-10-15','2026-10-15 14:18:43','2026-10-15 14:18:43','Entrada por Recepcion de Mercancia #89. werwerew','2026-10-15 14:18:43',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(309,'REC-90',9,NULL,'2026-10-15','2026-10-15 14:20:13','2026-10-15 14:20:13','Entrada por Recepcion de Mercancia #90. mecancia incompleta','2026-10-15 14:20:13',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(310,'REC-91',9,NULL,'2026-10-15','2026-10-15 14:25:30','2026-10-15 14:25:30','Entrada por Recepcion de Mercancia #91. se recibio una caja de leche de tres que se pideiron','2026-10-15 14:25:30',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(311,'REC-92',9,NULL,'2026-10-15','2026-10-15 15:10:08','2026-10-15 15:10:08','Entrada por Recepcion de Mercancia #92. eqweqwe','2026-10-15 15:10:08',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(312,'AJ-75',3,NULL,'2026-10-15','2026-10-15 15:27:41','2026-10-15 15:27:41','Entrada por Traslado de Mercancia #75. ','2026-10-15 15:27:41',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(313,'REC-93',9,NULL,'2026-10-15','2026-10-15 15:33:44','2026-10-15 15:33:44','Entrada por Recepcion de Mercancia #93. entrada de leche nido','2026-10-15 15:33:44',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(314,'AJ-76',3,NULL,'2026-10-15','2026-10-15 15:34:24','2026-10-15 15:34:24','Entrada por Traslado de Mercancia #76. ','2026-10-15 15:34:24',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(315,'AJ-77',3,NULL,'2026-10-15','2026-10-15 17:52:10','2026-10-15 17:52:10','Entrada por Traslado de Mercancia #77. ','2026-10-15 17:52:10',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(316,'AJ-89',3,NULL,'2026-10-15','2026-10-15 19:11:32','2026-10-15 19:11:32','Entrada por Ajuste de Inventario #89. a cero','2026-10-15 19:11:32',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(317,'AJ-78',3,NULL,'2026-10-16','2026-10-16 15:36:32','2026-10-16 15:36:32','Entrada por Traslado de Mercancia #78. ','2026-10-16 15:36:32',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(318,'AJ-79',3,NULL,'2026-10-16','2026-10-16 15:38:09','2026-10-16 15:38:09','Entrada por Traslado de Mercancia #79. ','2026-10-16 15:38:09',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(319,'AJ-80',3,NULL,'2026-10-17','2026-10-17 15:25:55','2026-10-17 15:25:55','Entrada por Traslado de Mercancia #80. ','2026-10-17 15:25:55',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(320,'AN-176',6,NULL,'2026-10-17','2026-10-17 15:35:38','2026-10-17 15:35:38','Entrada por anulacion de venta #176. ','2026-10-17 15:35:38',1,'DOP',_binary '\0',NULL,NULL,'Administrador',NULL),(321,'FACT-179',7,NULL,'2026-10-17','2026-10-17 15:38:25','2026-10-17 15:38:25','DevoluciÃ³n Total de Factura #179. Motivo: DEVOLUCION TOTAL','2026-10-17 15:38:25',1,'DOP',_binary '\0',NULL,NULL,'ADMIN',NULL),(322,'AJ-81',3,NULL,'2026-10-19','2026-10-19 17:16:34','2026-10-19 17:16:34','Entrada por Traslado de Mercancia #81. ','2026-10-19 17:16:34',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(323,'REC-94',9,NULL,'2026-10-19','2026-10-19 17:21:36','2026-10-19 17:21:36','Entrada por Recepcion de Mercancia #94. entrada de platano','2026-10-19 17:21:36',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(324,'AJ-82',3,NULL,'2026-10-19','2026-10-19 17:24:07','2026-10-19 17:24:07','Entrada por Traslado de Mercancia #82. ','2026-10-19 17:24:07',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `entrada_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_documento`
--

DROP TABLE IF EXISTS `estado_documento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_documento` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `nombre_modulo` varchar(30) NOT NULL,
  `color_badge` varchar(20) DEFAULT 'primary',
  `modulo` int DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_documento`
--

LOCK TABLES `estado_documento` WRITE;
/*!40000 ALTER TABLE `estado_documento` DISABLE KEYS */;
INSERT INTO `estado_documento` VALUES (1,'Pendiente','ORDEN_COMPRA','warning',2),(2,'Recibido Parcial','ORDEN_COMPRA','contrast',2),(3,'Completada','ORDEN_COMPRA','success',2),(4,'Anulada','ORDEN_COMPRA','error',2),(5,'Procesado','RECEPCION','success',2);
/*!40000 ALTER TABLE `estado_documento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_factura`
--

DROP TABLE IF EXISTS `estado_factura`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_factura` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_factura`
--

LOCK TABLES `estado_factura` WRITE;
/*!40000 ALTER TABLE `estado_factura` DISABLE KEYS */;
INSERT INTO `estado_factura` VALUES (1,'ABIERTA'),(2,'CERRADA');
/*!40000 ALTER TABLE `estado_factura` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `factura_de_venta`
--

DROP TABLE IF EXISTS `factura_de_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factura_de_venta` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `numero_documento` int DEFAULT NULL,
  `estado_factura` int DEFAULT NULL,
  `tipo_venta` int DEFAULT NULL,
  `tipo_documento` int DEFAULT NULL,
  `secuencia_documento` int DEFAULT NULL,
  `tipo_ncf` int DEFAULT NULL,
  `ncf` varchar(25) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `cliente` int DEFAULT NULL,
  `nombre_cliente` varchar(200) DEFAULT NULL,
  `delivery` int DEFAULT NULL,
  `nombre_delivery` varchar(60) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT NULL,
  `fecha_actualizacion` timestamp NULL DEFAULT NULL,
  `comentario` text CHARACTER SET latin1 COLLATE latin1_swedish_ci,
  `anulada` bit(1) DEFAULT NULL,
  `fecha_anulada` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario` int DEFAULT NULL,
  `nombre_usuario` varchar(80) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `anulada_por` varchar(80) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `sub_total` decimal(12,2) DEFAULT NULL,
  `total_descuento` decimal(12,2) DEFAULT NULL,
  `porciento_descuento` decimal(12,2) DEFAULT NULL,
  `total_itbis` decimal(12,2) DEFAULT NULL,
  `porciento_itbis` decimal(12,2) DEFAULT NULL,
  `total` decimal(12,2) DEFAULT NULL,
  `total_abonado` decimal(12,2) DEFAULT NULL,
  `total_pendiente` decimal(12,2) DEFAULT NULL,
  `direccion` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_fact_venta_idx` (`cliente`),
  KEY `fk_factura_estado_idx` (`estado_factura`),
  KEY `fk_factura_tipo_vanta_idx` (`tipo_venta`),
  KEY `fk_factura_delivery_idx` (`delivery`),
  CONSTRAINT `fk_fact_venta_cliente` FOREIGN KEY (`cliente`) REFERENCES `cliente` (`codigo`),
  CONSTRAINT `fk_factura_delivery` FOREIGN KEY (`delivery`) REFERENCES `delivery` (`codigo`),
  CONSTRAINT `fk_factura_estado` FOREIGN KEY (`estado_factura`) REFERENCES `estado_factura` (`codigo`),
  CONSTRAINT `fk_factura_tipo_vanta` FOREIGN KEY (`tipo_venta`) REFERENCES `tipo_venta` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=198 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `factura_de_venta`
--

LOCK TABLES `factura_de_venta` WRITE;
/*!40000 ALTER TABLE `factura_de_venta` DISABLE KEYS */;
INSERT INTO `factura_de_venta` VALUES (142,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-14','2026-10-14 14:01:02','2026-10-14 14:01:46',NULL,_binary '','2026-10-14 14:01:46',NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,30.00,NULL,NULL,'EMPRESA'),(143,NULL,2,2,NULL,NULL,NULL,NULL,15,'JOSE ALMANZAR',NULL,'N/A','2026-10-14','2026-10-14 14:05:28','2026-10-14 14:07:30',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,45.00,NULL,NULL,'25-10 LAS COLINAS SANTIAGO'),(144,NULL,2,2,NULL,NULL,NULL,NULL,14,'MAXIMILIANO ALMONTE RODRIGUEZ',NULL,'N/A','2026-10-14','2026-10-14 14:09:53','2026-10-14 14:11:34',NULL,_binary '','2026-10-14 14:11:34',NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,60.00,NULL,NULL,'Las colinas santiago  c/29 #5'),(145,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-14','2026-10-14 14:18:43','2026-10-14 14:18:43',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,210.00,NULL,NULL,'EMPRESA'),(146,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-14','2026-10-14 14:20:07','2026-10-14 14:23:19',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,985.00,NULL,NULL,'EMPRESA'),(147,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-14','2026-10-14 14:26:17','2026-10-14 14:26:17',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,1000.00,NULL,NULL,'EMPRESA'),(148,NULL,2,2,NULL,NULL,NULL,NULL,15,'JOSE ALMANZAR',NULL,'N/A','2026-10-14','2026-10-14 14:26:58','2026-10-14 14:26:58',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,1625.00,NULL,NULL,'25-10 LAS COLINAS SANTIAGO'),(149,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-14','2026-10-14 14:33:24','2026-10-14 14:47:22',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,1160.00,NULL,NULL,'EMPRESA'),(150,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-14','2026-10-14 14:48:31','2026-10-14 14:49:22',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,110.00,NULL,NULL,'EMPRESA'),(152,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-14','2026-10-14 15:48:13','2026-10-14 15:49:35',NULL,_binary '','2026-10-14 15:49:35',NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,170.00,NULL,NULL,'EMPRESA'),(153,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-14','2026-10-14 17:35:03','2026-10-14 17:35:03',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,800.00,NULL,NULL,'EMPRESA'),(154,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-14','2026-10-14 17:37:30','2026-10-14 17:54:55',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,1010.00,NULL,NULL,'EMPRESA'),(155,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-14','2026-10-14 17:54:30','2026-10-14 17:54:30',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,160.00,NULL,NULL,'EMPRESA'),(156,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-14','2026-10-14 17:57:58','2026-10-14 17:59:06',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,210.00,NULL,NULL,'EMPRESA'),(157,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-14','2026-10-14 18:01:49','2026-10-14 18:02:54',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,520.00,NULL,NULL,'EMPRESA'),(158,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-14','2026-10-14 18:07:38','2026-10-14 18:08:42',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,330.00,NULL,NULL,'EMPRESA'),(159,NULL,2,2,NULL,NULL,NULL,NULL,15,'JOSE ALMANZAR',NULL,'N/A','2026-10-15','2026-10-15 15:19:27','2026-10-15 15:19:27',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,160.00,NULL,NULL,'25-10 LAS COLINAS SANTIAGO'),(160,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-15','2026-10-15 15:35:38','2026-10-15 15:35:38',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,90.00,NULL,NULL,'EMPRESA'),(162,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-15','2026-10-15 17:12:19','2026-10-15 17:12:19',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,2400.00,NULL,NULL,'EMPRESA'),(164,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-15','2026-10-15 17:52:26','2026-10-15 17:52:26',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,880.00,NULL,NULL,'EMPRESA'),(166,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-15','2026-10-15 19:01:59','2026-10-15 19:01:59',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,720.00,NULL,NULL,'EMPRESA'),(168,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-15','2026-10-15 19:11:49','2026-10-15 19:11:49',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,30.00,NULL,NULL,'EMPRESA'),(169,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-16','2026-10-16 15:34:17','2026-10-16 15:34:17',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,700.00,NULL,NULL,'EMPRESA'),(170,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-16','2026-10-16 15:36:51','2026-10-16 15:36:51',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,700.00,NULL,NULL,'EMPRESA'),(172,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-16','2026-10-16 15:38:33','2026-10-16 15:38:33',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,700.00,NULL,NULL,'EMPRESA'),(173,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-16','2026-10-16 15:43:24','2026-10-16 15:43:24',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,210.00,NULL,NULL,'EMPRESA'),(174,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-16','2026-10-16 15:44:55','2026-10-16 15:44:55',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,70.00,NULL,NULL,'EMPRESA'),(175,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-16','2026-10-16 15:46:57','2026-10-16 15:46:57',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,70.00,NULL,NULL,'EMPRESA'),(176,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-17','2026-10-17 15:31:06','2026-10-17 15:35:38',NULL,_binary '','2026-10-17 15:35:38',NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,600.00,NULL,NULL,'EMPRESA'),(179,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-17','2026-10-17 15:37:28','2026-10-17 15:38:25',NULL,_binary '','2026-10-17 15:38:25',NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,60.00,NULL,NULL,'EMPRESA'),(181,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-17','2026-10-17 16:36:41','2026-10-17 16:36:41',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,700.00,NULL,NULL,'EMPRESA'),(183,NULL,2,2,NULL,NULL,NULL,NULL,14,'MAXIMILIANO ALMONTE RODRIGUEZ',NULL,'N/A','2026-10-17','2026-10-17 16:41:10','2026-10-17 16:41:10',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,15.00,NULL,NULL,'Las colinas santiago  c/29 #5'),(185,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-17','2026-10-17 20:25:49','2026-10-17 20:25:49',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,30.00,NULL,NULL,'EMPRESA'),(187,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-18','2026-10-18 15:43:10','2026-10-18 15:43:10',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,90.00,NULL,NULL,'EMPRESA'),(189,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-18','2026-10-18 15:47:01','2026-10-18 15:47:01',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,300.00,NULL,NULL,'EMPRESA'),(193,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-19','2026-10-19 15:44:19','2026-10-19 15:44:19',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,720.00,NULL,NULL,'EMPRESA'),(194,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-19','2026-10-19 17:17:05','2026-10-19 17:17:05',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,90.00,NULL,NULL,'EMPRESA'),(195,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-10-19','2026-10-19 17:18:56','2026-10-19 17:18:56',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,280.00,NULL,NULL,'EMPRESA'),(197,NULL,2,2,NULL,NULL,NULL,NULL,14,'MAXIMILIANO ALMONTE RODRIGUEZ',NULL,'N/A','2026-10-19','2026-10-19 17:24:43','2026-10-19 17:24:43',NULL,_binary '\0',NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,120.00,NULL,NULL,'Las colinas santiago  c/29 #5');
/*!40000 ALTER TABLE `factura_de_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merma`
--

DROP TABLE IF EXISTS `merma`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merma` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `fecha_registro` timestamp NOT NULL,
  `almacen` int NOT NULL,
  `tipo_merma` int NOT NULL,
  `observacion` varchar(500) DEFAULT NULL,
  `usuario` int DEFAULT NULL,
  `nombre_usuario` varchar(100) DEFAULT NULL,
  `salida_inventario` int DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_merma_salida_inventario` (`salida_inventario`),
  KEY `fk_merma_almacen` (`almacen`),
  KEY `fk_merma_tipo_merma` (`tipo_merma`),
  KEY `fk_merma_usuario_idx` (`usuario`),
  CONSTRAINT `fk_merma_almacen` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_merma_salida_inventario` FOREIGN KEY (`salida_inventario`) REFERENCES `salida_inventario` (`codigo`),
  CONSTRAINT `fk_merma_tipo_merma` FOREIGN KEY (`tipo_merma`) REFERENCES `tipo_merma` (`codigo`),
  CONSTRAINT `fk_merma_usuario` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merma`
--

LOCK TABLES `merma` WRITE;
/*!40000 ALTER TABLE `merma` DISABLE KEYS */;
INSERT INTO `merma` VALUES (1,'2026-10-19','2026-10-19 16:52:39',1,1,'vencimiento',NULL,'ADMIN',NULL),(2,'2026-10-19','2026-10-19 17:09:58',2,2,'rotura de articulo',NULL,'ADMIN',NULL),(3,'2026-10-19','2026-10-19 17:26:04',2,2,'SE DANARON',NULL,'ADMIN',NULL),(4,'2026-10-19','2026-10-19 17:57:50',2,2,'se pudieron',1,'ADMIN',NULL);
/*!40000 ALTER TABLE `merma` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `modulo`
--

DROP TABLE IF EXISTS `modulo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `modulo` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(30) NOT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modulo`
--

LOCK TABLES `modulo` WRITE;
/*!40000 ALTER TABLE `modulo` DISABLE KEYS */;
INSERT INTO `modulo` VALUES (1,'INVENTARIO'),(2,'COMPRA'),(3,'VENTA'),(4,'FINANZAS');
/*!40000 ALTER TABLE `modulo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `moneda`
--

DROP TABLE IF EXISTS `moneda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `moneda` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(30) NOT NULL,
  `simbolo` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `moneda`
--

LOCK TABLES `moneda` WRITE;
/*!40000 ALTER TABLE `moneda` DISABLE KEYS */;
/*!40000 ALTER TABLE `moneda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimiento_inventario`
--

DROP TABLE IF EXISTS `movimiento_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimiento_inventario` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `articulo` int NOT NULL,
  `almacen` int NOT NULL,
  `tipo_movimiento` int NOT NULL COMMENT 'ENTRADA, SALIDA, AJUSTE_INCREMENTO, AJUSTE_DECREMENTO',
  `tipo_documento` int NOT NULL COMMENT 'COMPRA, VENTA, AJUSTE, DEVOLUCION',
  `numero_documento` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Ej: AJ-2026-0001, FAC-10023',
  `cantidad` decimal(12,2) NOT NULL,
  `existencia_anterior` decimal(12,2) NOT NULL,
  `existencia_nueva` decimal(12,2) NOT NULL,
  `fecha_movimiento` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `observacion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `articulo_empaque` int DEFAULT NULL,
  `factor_conversion` decimal(12,2) DEFAULT NULL,
  `cantidad_empaque` decimal(12,2) DEFAULT NULL,
  `sub_total` decimal(12,2) DEFAULT NULL,
  `itbis` decimal(12,2) DEFAULT NULL,
  `total` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `idx_mov_inv_articulo_fecha` (`articulo`,`fecha_movimiento` DESC),
  KEY `idx_mov_inv_fecha` (`fecha_movimiento`),
  KEY `idx_mov_inv_documento` (`tipo_documento`,`numero_documento`),
  KEY `fk_mov_inv_tp_mov_idx` (`tipo_movimiento`),
  KEY `fk_mov_inv_almacen_idx` (`almacen`),
  KEY `fk_mov_inv_art_empaque_idx` (`articulo_empaque`),
  CONSTRAINT `fk_mov_inv_almacen` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_mov_inv_art_empaque` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_mov_inv_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_mov_inv_tp_docu` FOREIGN KEY (`tipo_documento`) REFERENCES `tipo_documento` (`codigo`),
  CONSTRAINT `fk_mov_inv_tp_mov` FOREIGN KEY (`tipo_movimiento`) REFERENCES `tipo_movimiento` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=400 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimiento_inventario`
--

LOCK TABLES `movimiento_inventario` WRITE;
/*!40000 ALTER TABLE `movimiento_inventario` DISABLE KEYS */;
INSERT INTO `movimiento_inventario` VALUES (229,4,2,2,2,'SAL-142',2.00,15.00,13.00,'2026-10-14 10:01:03','Administrador','Salida automÃ¡tica por Factura POS #142',68,1.00,2.00,20.00,0.00,20.00),(230,4,2,1,1,'ENT-262',2.00,13.00,15.00,'2026-10-14 10:01:46','Administrador','Entrada por anulacion de venta #142. ',68,1.00,2.00,20.00,0.00,20.00),(231,4,1,1,1,'ENT-263',10.00,0.00,10.00,'2026-10-14 10:03:46','SISTEMA','Entrada por Recepcion de Mercancia #56. entrada de mercancia',68,1.00,10.00,100.00,0.00,100.00),(232,4,2,1,1,'ENT-264',5.00,15.00,20.00,'2026-10-14 10:04:46','SISTEMA','Entrada por Traslado de Mercancia #71. ',68,1.00,5.00,50.00,0.00,50.00),(233,4,1,2,2,'SAL-71',5.00,10.00,5.00,'2026-10-14 10:04:46','SISTEMA','Salida por Traslado de Inventario #71. ',68,1.00,5.00,50.00,0.00,50.00),(234,4,2,2,2,'SAL-143',3.00,20.00,17.00,'2026-10-14 10:05:28','Administrador','Salida automÃ¡tica por Factura POS #143',68,1.00,3.00,30.00,0.00,30.00),(235,4,2,1,1,'ENT-265',1.00,17.00,18.00,'2026-10-14 10:07:30','ADMIN','DevoluciÃ³n Parcial de Factura #143. Motivo: devolucion parcial',68,1.00,1.00,10.00,0.00,10.00),(236,4,2,1,1,'143',1.00,18.00,19.00,'2026-10-14 10:07:30','ADMIN','DevoluciÃ³n Parcial Venta #143. Motivo: devolucion parcial',NULL,1.00,1.00,0.00,0.00,0.00),(237,4,2,2,2,'SAL-144',4.00,19.00,15.00,'2026-10-14 10:09:54','Administrador','Salida automÃ¡tica por Factura POS #144',68,1.00,4.00,40.00,0.00,40.00),(238,4,2,1,1,'ENT-266',2.00,15.00,17.00,'2026-10-14 10:11:01','ADMIN','DevoluciÃ³n Parcial de Factura #144. Motivo: dev parcial',68,1.00,2.00,20.00,0.00,20.00),(239,4,2,1,1,'144',2.00,17.00,19.00,'2026-10-14 10:11:01','ADMIN','DevoluciÃ³n Parcial Venta #144. Motivo: dev parcial',NULL,1.00,2.00,0.00,0.00,0.00),(240,4,2,1,1,'ENT-267',4.00,19.00,23.00,'2026-10-14 10:11:34','Administrador','Entrada por anulacion de venta #144. ',68,1.00,4.00,40.00,0.00,40.00),(241,2,2,2,2,'SAL-145',3.00,60.00,57.00,'2026-10-14 10:18:43','Administrador','Salida automÃ¡tica por Factura POS #145',62,1.00,3.00,240.00,0.00,240.00),(242,4,2,2,2,'SAL-146',3.00,23.00,20.00,'2026-10-14 10:20:07','Administrador','Salida automÃ¡tica por Factura POS #146',68,1.00,3.00,30.00,0.00,30.00),(243,2,2,2,2,'SAL-146',2.00,57.00,55.00,'2026-10-14 10:20:07','Administrador','Salida automÃ¡tica por Factura POS #146',62,1.00,2.00,160.00,0.00,160.00),(244,33,2,2,2,'SAL-146',10.00,19.00,9.00,'2026-10-14 10:20:07','Administrador','Salida automÃ¡tica por Factura POS #146',51,1.00,10.00,60.00,0.00,60.00),(245,4,2,1,1,'ENT-268',1.00,20.00,21.00,'2026-10-14 10:23:19','ADMIN','DevoluciÃ³n Parcial de Factura #146. Motivo: devolcucion del aceite',68,1.00,1.00,10.00,0.00,10.00),(246,2,2,1,1,'ENT-268',1.00,55.00,56.00,'2026-10-14 10:23:19','ADMIN','DevoluciÃ³n Parcial de Factura #146. Motivo: devolcucion del aceite',62,1.00,1.00,80.00,0.00,80.00),(247,33,2,1,1,'ENT-268',10.00,9.00,19.00,'2026-10-14 10:23:19','ADMIN','DevoluciÃ³n Parcial de Factura #146. Motivo: devolcucion del aceite',51,1.00,10.00,60.00,0.00,60.00),(248,4,2,1,1,'146',1.00,21.00,22.00,'2026-10-14 10:23:19','ADMIN','DevoluciÃ³n Parcial Venta #146. Motivo: devolcucion del aceite',NULL,1.00,1.00,0.00,0.00,0.00),(249,2,2,1,1,'146',1.00,56.00,57.00,'2026-10-14 10:23:19','ADMIN','DevoluciÃ³n Parcial Venta #146. Motivo: devolcucion del aceite',NULL,1.00,1.00,0.00,0.00,0.00),(250,2,2,2,2,'SAL-147',10.00,57.00,47.00,'2026-10-14 10:26:17','Administrador','Salida automÃ¡tica por Factura POS #147',62,1.00,10.00,80.00,0.00,80.00),(251,2,2,2,2,'SAL-147',5.00,47.00,42.00,'2026-10-14 10:26:17','Administrador','Salida automÃ¡tica por Factura POS #147',62,1.00,5.00,80.00,0.00,80.00),(252,33,2,2,2,'SAL-148',10.00,19.00,9.00,'2026-10-14 10:26:58','Administrador','Salida automÃ¡tica por Factura POS #148',51,1.00,10.00,60.00,0.00,60.00),(253,4,2,2,2,'SAL-148',1.00,22.00,21.00,'2026-10-14 10:26:58','Administrador','Salida automÃ¡tica por Factura POS #148',68,1.00,1.00,10.00,0.00,10.00),(254,34,2,2,2,'SAL-148',1.00,13.00,12.00,'2026-10-14 10:26:58','Administrador','Salida automÃ¡tica por Factura POS #148',66,1.00,1.00,25.00,0.00,25.00),(255,2,2,2,2,'SAL-148',10.00,42.00,32.00,'2026-10-14 10:26:58','Administrador','Salida automÃ¡tica por Factura POS #148',62,1.00,10.00,80.00,0.00,80.00),(256,33,2,2,2,'SAL-148',1.00,9.00,8.00,'2026-10-14 10:26:58','Administrador','Salida automÃ¡tica por Factura POS #148',51,1.00,1.00,60.00,0.00,60.00),(257,33,2,2,2,'SAL-149',2.00,8.00,6.00,'2026-10-14 10:33:24','Administrador','Salida automÃ¡tica por Factura POS #149',51,1.00,2.00,120.00,0.00,120.00),(258,2,2,2,2,'SAL-149',10.00,32.00,22.00,'2026-10-14 10:33:24','Administrador','Salida automÃ¡tica por Factura POS #149',62,1.00,10.00,80.00,0.00,80.00),(259,2,2,2,2,'SAL-149',5.00,22.00,17.00,'2026-10-14 10:33:24','Administrador','Salida automÃ¡tica por Factura POS #149',62,1.00,5.00,80.00,0.00,80.00),(260,2,2,1,1,'ENT-269',5.00,17.00,22.00,'2026-10-14 10:47:22','ADMIN','DevoluciÃ³n Parcial de Factura #149. Motivo: sfsfsd',62,1.00,5.00,80.00,0.00,80.00),(261,2,2,1,1,'149',1.00,22.00,23.00,'2026-10-14 10:47:22','ADMIN','DevoluciÃ³n Parcial Venta #149. Motivo: sfsfsd',NULL,1.00,1.00,0.00,0.00,0.00),(262,33,2,2,2,'SAL-150',1.00,6.00,5.00,'2026-10-14 10:48:31','Administrador','Salida automÃ¡tica por Factura POS #150',51,1.00,1.00,60.00,0.00,60.00),(263,4,2,2,2,'SAL-150',2.00,21.00,19.00,'2026-10-14 10:48:31','Administrador','Salida automÃ¡tica por Factura POS #150',68,1.00,2.00,20.00,0.00,20.00),(264,33,2,1,1,'ENT-270',1.00,5.00,6.00,'2026-10-14 10:49:22','ADMIN','DevoluciÃ³n Parcial de Factura #150. Motivo: devolucion del aceite',51,1.00,1.00,60.00,0.00,60.00),(266,34,1,1,1,'ENT-271',20.00,7.00,27.00,'2026-10-14 11:35:15','SISTEMA','Entrada por Ajuste de Inventario #86. incremento ',66,1.00,20.00,50.00,0.00,50.00),(267,34,1,2,2,'SAL-87',10.00,27.00,17.00,'2026-10-14 11:36:03','SISTEMA','Salida por Ajuste de Inventario #87. ajuste',66,1.00,10.00,25.00,0.00,25.00),(268,34,2,1,1,'ENT-272',10.00,12.00,22.00,'2026-10-14 11:38:45','SISTEMA','Entrada por Traslado de Mercancia #72. ',66,1.00,10.00,25.00,0.00,25.00),(269,34,1,2,2,'SAL-72',10.00,17.00,7.00,'2026-10-14 11:38:45','SISTEMA','Salida por Traslado de Inventario #72. ',66,1.00,10.00,25.00,0.00,25.00),(270,33,2,2,2,'SAL-152',1.00,6.00,5.00,'2026-10-14 11:48:13','Administrador','Salida automÃ¡tica por Factura POS #152',51,1.00,1.00,60.00,0.00,60.00),(271,34,2,2,2,'SAL-152',3.00,22.00,19.00,'2026-10-14 11:48:13','Administrador','Salida automÃ¡tica por Factura POS #152',66,1.00,3.00,75.00,0.00,75.00),(272,33,2,1,1,'ENT-273',1.00,5.00,6.00,'2026-10-14 11:49:35','Administrador','Entrada por anulacion de venta #152. ',51,1.00,1.00,60.00,0.00,60.00),(273,34,2,1,1,'ENT-273',3.00,19.00,22.00,'2026-10-14 11:49:35','Administrador','Entrada por anulacion de venta #152. ',66,1.00,3.00,75.00,0.00,75.00),(274,33,2,2,2,'SAL-153',10.00,6.00,-4.00,'2026-10-14 13:35:03','Administrador','Salida automÃ¡tica por Factura POS #153',51,1.00,10.00,60.00,0.00,60.00),(275,33,2,1,1,'ENT-274',4.00,-4.00,0.00,'2026-10-14 13:36:10','SISTEMA','Entrada por Ajuste de Inventario #88. aumento 4',51,1.00,4.00,240.00,0.00,240.00),(276,33,2,1,1,'ENT-275',10.00,0.00,10.00,'2026-10-14 13:36:48','SISTEMA','Entrada por Traslado de Mercancia #73. ',51,1.00,10.00,60.00,0.00,60.00),(277,33,1,2,2,'SAL-73',10.00,283.00,273.00,'2026-10-14 13:36:48','SISTEMA','Salida por Traslado de Inventario #73. ',51,1.00,10.00,60.00,0.00,60.00),(278,33,2,2,2,'SAL-154',10.00,10.00,0.00,'2026-10-14 13:37:30','Administrador','Salida automÃ¡tica por Factura POS #154',51,1.00,10.00,60.00,0.00,60.00),(279,2,2,2,2,'SAL-154',3.00,23.00,20.00,'2026-10-14 13:37:30','Administrador','Salida automÃ¡tica por Factura POS #154',62,1.00,3.00,240.00,0.00,240.00),(280,33,2,1,1,'ENT-276',10.00,0.00,10.00,'2026-10-14 13:38:50','ADMIN','DevoluciÃ³n Parcial de Factura #154. Motivo: devolucion de la caja de aceite',51,1.00,10.00,60.00,0.00,60.00),(281,33,2,2,2,'SAL-155',2.00,10.00,8.00,'2026-10-14 13:54:30','Administrador','Salida automÃ¡tica por Factura POS #155',51,1.00,2.00,120.00,0.00,120.00),(282,2,2,1,1,'ENT-277',1.00,20.00,21.00,'2026-10-14 13:54:55','ADMIN','DevoluciÃ³n Parcial de Factura #154. Motivo: dev una',62,1.00,1.00,80.00,0.00,80.00),(283,2,2,2,2,'SAL-156',3.00,21.00,18.00,'2026-10-14 13:57:59','Administrador','Salida automÃ¡tica por Factura POS #156',62,1.00,3.00,240.00,0.00,240.00),(284,2,2,1,1,'ENT-278',1.00,18.00,19.00,'2026-10-14 13:59:06','ADMIN','DevoluciÃ³n Parcial de Factura #156. Motivo: una',62,1.00,1.00,80.00,0.00,80.00),(285,33,2,1,1,'ENT-279',20.00,8.00,28.00,'2026-10-14 14:01:01','SISTEMA','Entrada por Traslado de Mercancia #74. ',51,1.00,20.00,120.00,0.00,120.00),(286,33,1,2,2,'SAL-74',20.00,273.00,253.00,'2026-10-14 14:01:01','SISTEMA','Salida por Traslado de Inventario #74. ',51,1.00,20.00,120.00,0.00,120.00),(287,2,2,2,2,'SAL-157',4.00,19.00,15.00,'2026-10-14 14:01:49','Administrador','Salida automÃ¡tica por Factura POS #157',62,1.00,4.00,320.00,0.00,320.00),(288,33,2,2,2,'SAL-157',3.00,28.00,25.00,'2026-10-14 14:01:49','Administrador','Salida automÃ¡tica por Factura POS #157',51,1.00,3.00,180.00,0.00,180.00),(289,2,2,1,1,'ENT-280',1.00,15.00,16.00,'2026-10-14 14:02:54','ADMIN','DevoluciÃ³n Parcial de Factura #157. Motivo: dev',62,1.00,1.00,80.00,0.00,80.00),(290,33,2,1,1,'ENT-280',1.00,25.00,26.00,'2026-10-14 14:02:54','ADMIN','DevoluciÃ³n Parcial de Factura #157. Motivo: dev',51,1.00,1.00,60.00,0.00,60.00),(291,33,2,2,2,'SAL-158',2.00,26.00,24.00,'2026-10-14 14:07:38','Administrador','Salida automÃ¡tica por Factura POS #158',51,1.00,2.00,120.00,0.00,120.00),(292,2,2,2,2,'SAL-158',2.00,16.00,14.00,'2026-10-14 14:07:38','Administrador','Salida automÃ¡tica por Factura POS #158',62,1.00,2.00,160.00,0.00,160.00),(293,4,2,2,2,'SAL-158',2.00,19.00,17.00,'2026-10-14 14:07:38','Administrador','Salida automÃ¡tica por Factura POS #158',68,1.00,2.00,20.00,0.00,20.00),(294,2,2,1,1,'ENT-281',2.00,14.00,16.00,'2026-10-14 14:08:42','ADMIN','DevoluciÃ³n Parcial de Factura #158. Motivo: devolucioon de la leche',62,1.00,2.00,160.00,0.00,160.00),(295,1,1,1,1,'ENT-282',1.00,10.00,11.00,'2026-10-14 16:30:50','SISTEMA','Entrada por Recepcion de Mercancia #59. dsadasdasd',53,1.00,1.00,50.00,0.00,50.00),(296,2,1,1,1,'ENT-283',10.00,92.00,102.00,'2026-10-14 16:31:34','SISTEMA','Entrada por Recepcion de Mercancia #60. erwerwerwerewr',62,1.00,10.00,800.00,0.00,800.00),(297,34,1,1,1,'ENT-283',5.00,7.00,12.00,'2026-10-14 16:31:34','SISTEMA','Entrada por Recepcion de Mercancia #60. erwerwerwerewr',66,1.00,5.00,125.00,0.00,125.00),(298,6,1,1,1,'ENT-284',2.00,0.00,2.00,'2026-10-14 16:42:50','SISTEMA','Entrada por Recepcion de Mercancia #62. eerwerwerwewerwerwer',69,1.00,2.00,50.00,0.00,50.00),(299,6,1,1,1,'ENT-285',2.00,2.00,4.00,'2026-10-14 17:09:59','SISTEMA','Entrada por Recepcion de Mercancia #63. PROBANDO OC',69,1.00,2.00,50.00,0.00,50.00),(300,6,1,1,1,'ENT-286',2.00,4.00,6.00,'2026-10-14 17:28:43','SISTEMA','Entrada por Recepcion de Mercancia #66. wrwerwrew',69,1.00,2.00,50.00,0.00,50.00),(301,2,1,1,1,'ENT-287',10.00,102.00,112.00,'2026-10-14 17:29:06','SISTEMA','Entrada por Recepcion de Mercancia #67. werwerew',62,1.00,10.00,800.00,0.00,800.00),(302,34,1,1,1,'ENT-287',5.00,12.00,17.00,'2026-10-14 17:29:06','SISTEMA','Entrada por Recepcion de Mercancia #67. werwerew',66,1.00,5.00,125.00,0.00,125.00),(336,2,1,1,1,'ENT-303',3.00,112.00,115.00,'2026-10-15 09:52:11','SISTEMA','Entrada por Recepcion de Mercancia #83. werwerwe',62,1.00,3.00,240.00,0.00,240.00),(337,2,1,1,1,'ENT-304',1.00,115.00,116.00,'2026-10-15 09:57:12','SISTEMA','Entrada por Recepcion de Mercancia #84. werwrwerwe',62,1.00,1.00,80.00,0.00,80.00),(338,1,1,1,1,'ENT-305',1.00,11.00,12.00,'2026-10-15 10:01:08','SISTEMA','Entrada por Recepcion de Mercancia #86. wrwrwer',53,1.00,1.00,50.00,0.00,50.00),(339,6,1,1,1,'ENT-305',10.00,6.00,16.00,'2026-10-15 10:01:08','SISTEMA','Entrada por Recepcion de Mercancia #86. wrwrwer',69,1.00,10.00,250.00,0.00,250.00),(340,1,1,1,1,'ENT-306',10.00,12.00,22.00,'2026-10-15 10:13:34','SISTEMA','Entrada por Recepcion de Mercancia #87. rewrwerwer',53,1.00,10.00,500.00,0.00,500.00),(341,4,1,1,1,'ENT-306',3.00,5.00,8.00,'2026-10-15 10:13:34','SISTEMA','Entrada por Recepcion de Mercancia #87. rewrwerwer',68,1.00,3.00,30.00,0.00,30.00),(342,6,1,1,1,'ENT-306',900.00,16.00,916.00,'2026-10-15 10:13:34','SISTEMA','Entrada por Recepcion de Mercancia #87. rewrwerwer',69,1.00,900.00,75.00,0.00,75.00),(343,6,1,1,1,'ENT-307',300.00,916.00,1216.00,'2026-10-15 10:13:50','SISTEMA','Entrada por Recepcion de Mercancia #88. dwqeqweqwew',69,1.00,300.00,25.00,0.00,25.00),(344,33,1,1,1,'ENT-308',20.00,253.00,273.00,'2026-10-15 10:18:43','SISTEMA','Entrada por Recepcion de Mercancia #89. werwerew',51,1.00,20.00,120.00,0.00,120.00),(345,1,1,1,1,'ENT-309',1.00,22.00,23.00,'2026-10-15 10:20:13','SISTEMA','Entrada por Recepcion de Mercancia #90. mecancia incompleta',53,1.00,1.00,50.00,0.00,50.00),(346,4,1,1,1,'ENT-309',1.00,8.00,9.00,'2026-10-15 10:20:13','SISTEMA','Entrada por Recepcion de Mercancia #90. mecancia incompleta',68,1.00,1.00,10.00,0.00,10.00),(347,6,1,1,1,'ENT-309',300.00,1216.00,1516.00,'2026-10-15 10:20:13','SISTEMA','Entrada por Recepcion de Mercancia #90. mecancia incompleta',69,1.00,300.00,25.00,0.00,25.00),(348,2,1,1,1,'ENT-310',10.00,116.00,126.00,'2026-10-15 10:25:30','SISTEMA','Entrada por Recepcion de Mercancia #91. se recibio una caja de leche de tres que se pideiron',62,1.00,10.00,80.00,0.00,80.00),(349,2,1,1,1,'ENT-311',2.00,126.00,128.00,'2026-10-15 11:10:08','SISTEMA','Entrada por Recepcion de Mercancia #92. eqweqwe',62,1.00,2.00,160.00,0.00,160.00),(350,33,2,2,2,'SAL-159',2.00,24.00,22.00,'2026-10-15 11:19:27','Administrador','Salida automÃ¡tica por Factura POS #159',51,1.00,2.00,120.00,0.00,120.00),(351,34,2,1,1,'ENT-312',10.00,22.00,32.00,'2026-10-15 11:27:41','SISTEMA','Entrada por Traslado de Mercancia #75. ',66,1.00,10.00,25.00,0.00,25.00),(352,34,1,2,2,'SAL-75',10.00,17.00,7.00,'2026-10-15 11:27:42','SISTEMA','Salida por Traslado de Inventario #75. ',66,1.00,10.00,25.00,0.00,25.00),(353,5,1,1,1,'ENT-313',45.00,0.00,45.00,'2026-10-15 11:33:44','SISTEMA','Entrada por Recepcion de Mercancia #93. entrada de leche nido',71,1.00,45.00,105.00,0.00,105.00),(354,5,2,1,1,'ENT-314',15.00,0.00,15.00,'2026-10-15 11:34:24','SISTEMA','Entrada por Traslado de Mercancia #76. ',71,1.00,15.00,35.00,0.00,35.00),(355,5,1,2,2,'SAL-76',15.00,45.00,30.00,'2026-10-15 11:34:24','SISTEMA','Salida por Traslado de Inventario #76. ',71,1.00,15.00,35.00,0.00,35.00),(356,5,2,2,2,'SAL-160',2.00,15.00,13.00,'2026-10-15 11:35:38','Administrador','Salida automÃ¡tica por Factura POS #160',71,1.00,2.00,70.00,0.00,70.00),(357,33,2,2,2,'SAL-162',30.00,22.00,-8.00,'2026-10-15 13:12:19','Administrador','Salida automÃ¡tica por Factura POS #162',51,1.00,30.00,180.00,0.00,180.00),(358,33,2,1,1,'ENT-315',10.00,-8.00,2.00,'2026-10-15 13:52:10','SISTEMA','Entrada por Traslado de Mercancia #77. ',51,1.00,10.00,60.00,0.00,60.00),(359,33,1,2,2,'SAL-77',10.00,273.00,263.00,'2026-10-15 13:52:10','SISTEMA','Salida por Traslado de Inventario #77. ',51,1.00,10.00,60.00,0.00,60.00),(360,33,2,2,2,'SAL-164',1.00,2.00,1.00,'2026-10-15 13:52:26','Administrador','Salida automÃ¡tica por Factura POS #164',51,1.00,1.00,60.00,0.00,60.00),(361,33,2,2,2,'SAL-164',10.00,1.00,-9.00,'2026-10-15 13:52:26','Administrador','Salida automÃ¡tica por Factura POS #164',51,1.00,10.00,60.00,0.00,60.00),(362,3,2,2,2,'SAL-166',48.00,5.00,-43.00,'2026-10-15 15:01:59','Administrador','Salida automÃ¡tica por Factura POS #166',73,1.00,48.00,10.00,0.00,10.00),(363,3,2,1,1,'ENT-316',100.00,-43.00,57.00,'2026-10-15 15:11:32','SISTEMA','Entrada por Ajuste de Inventario #89. a cero',73,1.00,100.00,1000.00,0.00,1000.00),(364,3,2,2,2,'SAL-168',2.00,57.00,55.00,'2026-10-15 15:11:49','Administrador','Salida automÃ¡tica por Factura POS #168',73,1.00,2.00,20.00,0.00,20.00),(365,2,2,2,2,'SAL-169',10.00,16.00,6.00,'2026-10-16 11:34:17','Administrador','Salida automÃ¡tica por Factura POS #169',62,1.00,10.00,80.00,0.00,80.00),(366,2,2,1,1,'ENT-317',10.00,6.00,16.00,'2026-10-16 11:36:32','SISTEMA','Entrada por Traslado de Mercancia #78. ',62,1.00,10.00,80.00,0.00,80.00),(367,2,1,2,2,'SAL-78',10.00,128.00,118.00,'2026-10-16 11:36:32','SISTEMA','Salida por Traslado de Inventario #78. ',62,1.00,10.00,80.00,0.00,80.00),(368,2,2,2,2,'SAL-170',10.00,16.00,6.00,'2026-10-16 11:36:51','Administrador','Salida automÃ¡tica por Factura POS #170',62,1.00,10.00,80.00,0.00,80.00),(370,2,2,1,1,'ENT-318',30.00,6.00,36.00,'2026-10-16 11:38:09','SISTEMA','Entrada por Traslado de Mercancia #79. ',62,1.00,30.00,240.00,0.00,240.00),(371,2,1,2,2,'SAL-79',30.00,118.00,88.00,'2026-10-16 11:38:09','SISTEMA','Salida por Traslado de Inventario #79. ',62,1.00,30.00,240.00,0.00,240.00),(372,2,2,2,2,'SAL-172',10.00,36.00,26.00,'2026-10-16 11:38:33','Administrador','Salida automÃ¡tica por Factura POS #172',62,1.00,10.00,80.00,0.00,80.00),(373,2,2,2,2,'SAL-90',6.00,26.00,20.00,'2026-10-16 11:42:08','SISTEMA','Salida por Ajuste de Inventario #90. DISMINUCION DE 6 UN',62,1.00,6.00,480.00,0.00,480.00),(374,2,2,2,2,'SAL-173',3.00,20.00,17.00,'2026-10-16 11:43:24','Administrador','Salida automÃ¡tica por Factura POS #173',62,1.00,3.00,240.00,0.00,240.00),(375,2,2,2,2,'SAL-174',1.00,17.00,16.00,'2026-10-16 11:44:55','Administrador','Salida automÃ¡tica por Factura POS #174',62,1.00,1.00,80.00,0.00,80.00),(376,2,2,2,2,'SAL-175',1.00,16.00,15.00,'2026-10-16 11:46:57','Administrador','Salida automÃ¡tica por Factura POS #175',62,1.00,1.00,80.00,0.00,80.00),(377,3,2,1,1,'ENT-319',2.00,55.00,57.00,'2026-10-17 11:25:55','SISTEMA','Entrada por Traslado de Mercancia #80. ',73,1.00,2.00,20.00,0.00,20.00),(378,3,1,2,2,'SAL-80',2.00,4.00,2.00,'2026-10-17 11:25:55','SISTEMA','Salida por Traslado de Inventario #80. ',73,1.00,2.00,20.00,0.00,20.00),(379,2,2,2,2,'SAL-176',10.00,15.00,5.00,'2026-10-17 11:31:06','Administrador','Salida automÃ¡tica por Factura POS #176',62,1.00,10.00,160.00,0.00,160.00),(380,2,2,1,1,'ENT-320',10.00,5.00,15.00,'2026-10-17 11:35:38','Administrador','Entrada por anulacion de venta #176. ',62,1.00,10.00,160.00,0.00,160.00),(381,1,2,2,2,'SAL-179',1.00,3.00,2.00,'2026-10-17 11:37:28','Administrador','Salida automÃ¡tica por Factura POS #179',53,1.00,1.00,50.00,0.00,50.00),(382,1,2,1,1,'ENT-321',1.00,2.00,3.00,'2026-10-17 11:38:25','ADMIN','DevoluciÃ³n Total de Factura #179. Motivo: DEVOLUCION TOTAL',53,1.00,1.00,50.00,0.00,50.00),(383,2,2,2,2,'SAL-181',10.00,15.00,5.00,'2026-10-17 12:36:41','Administrador','Salida automÃ¡tica por Factura POS #181',62,1.00,10.00,80.00,0.00,80.00),(384,3,2,2,2,'SAL-183',1.00,57.00,56.00,'2026-10-17 12:41:10','Administrador','Salida automÃ¡tica por Factura POS #183',73,1.00,1.00,10.00,0.00,10.00),(385,3,2,2,2,'SAL-185',2.00,56.00,54.00,'2026-10-17 16:25:49','Administrador','Salida automÃ¡tica por Factura POS #185',73,1.00,2.00,20.00,0.00,20.00),(386,5,2,2,2,'SAL-187',2.00,13.00,11.00,'2026-10-18 11:43:10','Administrador','Salida automÃ¡tica por Factura POS #187',71,1.00,2.00,70.00,0.00,70.00),(387,2,2,2,2,'SAL-189',5.00,5.00,0.00,'2026-10-18 11:47:01','Administrador','Salida automÃ¡tica por Factura POS #189',62,1.00,5.00,80.00,0.00,80.00),(389,3,2,2,2,'SAL-193',48.00,54.00,6.00,'2026-10-19 11:44:19','Administrador','Salida automÃ¡tica por Factura POS #193',73,1.00,48.00,10.00,0.00,10.00),(390,2,2,1,1,'ENT-322',20.00,0.00,20.00,'2026-10-19 13:16:34','SISTEMA','Entrada por Traslado de Mercancia #81. ',62,1.00,20.00,160.00,0.00,160.00),(391,2,1,2,2,'SAL-81',20.00,88.00,68.00,'2026-10-19 13:16:34','SISTEMA','Salida por Traslado de Inventario #81. ',62,1.00,20.00,160.00,0.00,160.00),(392,5,2,2,2,'SAL-194',2.00,11.00,9.00,'2026-10-19 13:17:05','Administrador','Salida automÃ¡tica por Factura POS #194',71,1.00,2.00,70.00,0.00,70.00),(393,2,2,2,2,'SAL-195',4.00,20.00,16.00,'2026-10-19 13:18:56','Administrador','Salida automÃ¡tica por Factura POS #195',62,1.00,4.00,320.00,0.00,320.00),(394,7,1,1,1,'ENT-323',50.00,0.00,50.00,'2026-10-19 13:21:36','SISTEMA','Entrada por Recepcion de Mercancia #94. entrada de platano',75,1.00,50.00,750.00,0.00,750.00),(395,7,2,1,1,'ENT-324',50.00,0.00,50.00,'2026-10-19 13:24:07','SISTEMA','Entrada por Traslado de Mercancia #82. ',75,1.00,50.00,600.00,0.00,600.00),(396,7,1,2,2,'SAL-82',50.00,50.00,0.00,'2026-10-19 13:24:07','SISTEMA','Salida por Traslado de Inventario #82. ',75,1.00,50.00,600.00,0.00,600.00),(397,7,2,2,2,'SAL-197',6.00,50.00,44.00,'2026-10-19 13:24:43','Administrador','Salida automÃ¡tica por Factura POS #197',75,1.00,6.00,72.00,0.00,72.00),(398,7,2,2,2,'SAL-91',5.00,44.00,39.00,'2026-10-19 13:27:28','SISTEMA','Salida por Ajuste de Inventario #91. POR MERMA',75,1.00,5.00,60.00,0.00,60.00),(399,7,2,2,2,'SAL-4',2.00,39.00,37.00,'2026-10-19 13:57:50','SISTEMA','Salida por Merma de Inventario #4. se pudieron',75,1.00,2.00,24.00,0.00,24.00);
/*!40000 ALTER TABLE `movimiento_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimientos_caja`
--

DROP TABLE IF EXISTS `movimientos_caja`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientos_caja` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `caja_turno` bigint NOT NULL,
  `tipo` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `monto` decimal(12,2) NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_hora` datetime NOT NULL,
  `usuario` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_movimiento_caja_turno` (`caja_turno`),
  CONSTRAINT `fk_movimiento_caja_turno` FOREIGN KEY (`caja_turno`) REFERENCES `caja_turno` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=126 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimientos_caja`
--

LOCK TABLES `movimientos_caja` WRITE;
/*!40000 ALTER TABLE `movimientos_caja` DISABLE KEYS */;
INSERT INTO `movimientos_caja` VALUES (98,36,'POS_CONTADO',330.00,'Ingreso por venta #158','2026-10-14 14:07:38','Administrador'),(99,36,'POS_CONTADO',-140.00,'DevoluciÃ³n Parcial Factura #158. Motivo: devolucioon de la leche','2026-10-14 14:08:42','ADMIN'),(100,36,'POS_CREDITO',160.00,'Ingreso por venta #159','2026-10-15 11:19:27','Administrador'),(101,36,'POS_CONTADO',90.00,'Ingreso por venta #160','2026-10-15 11:35:38','Administrador'),(102,36,'POS_CONTADO',2400.00,'Ingreso por venta #162','2026-10-15 13:12:19','Administrador'),(103,36,'POS_CONTADO',880.00,'Ingreso por venta #164','2026-10-15 13:52:26','Administrador'),(104,36,'POS_CONTADO',720.00,'Ingreso por venta #166','2026-10-15 15:01:59','Administrador'),(105,36,'POS_CONTADO',30.00,'Ingreso por venta #168','2026-10-15 15:11:49','Administrador'),(106,36,'POS_CONTADO',700.00,'Ingreso por venta #169','2026-10-16 11:34:17','Administrador'),(107,36,'POS_CONTADO',700.00,'Ingreso por venta #170','2026-10-16 11:36:51','Administrador'),(108,36,'POS_CONTADO',700.00,'Ingreso por venta #172','2026-10-16 11:38:33','Administrador'),(109,36,'POS_CONTADO',210.00,'Ingreso por venta #173','2026-10-16 11:43:24','Administrador'),(110,36,'POS_CONTADO',70.00,'Ingreso por venta #174','2026-10-16 11:44:55','Administrador'),(111,36,'POS_CONTADO',70.00,'Ingreso por venta #175','2026-10-16 11:46:57','Administrador'),(112,36,'POS_CONTADO',600.00,'Ingreso por venta #176','2026-10-17 11:31:06','Administrador'),(113,36,'POS_CONTADO',-600.00,'AnulaciÃ³n de Venta #176. Motivo: ANULAR VENTA','2026-10-17 11:35:38','Administrador'),(114,36,'POS_CONTADO',60.00,'Ingreso por venta #179','2026-10-17 11:37:28','Administrador'),(115,36,'POS_CONTADO',-60.00,'DevoluciÃ³n Total Factura #179. Motivo: DEVOLUCION TOTAL','2026-10-17 11:38:25','ADMIN'),(116,36,'POS_CONTADO',700.00,'Ingreso por venta #181','2026-10-17 12:36:41','Administrador'),(117,36,'POS_CREDITO',15.00,'Ingreso por venta #183','2026-10-17 12:41:10','Administrador'),(118,36,'POS_CONTADO',30.00,'Ingreso por venta #185','2026-10-17 16:25:49','Administrador'),(119,36,'POS_Consumo con Tarjeta / DatÃ¡fono',100.00,'qwewqewqe','2026-10-17 16:30:12','Administrador'),(120,36,'POS_CONTADO',90.00,'Ingreso por venta #187','2026-10-18 11:43:10','Administrador'),(121,36,'POS_CONTADO',300.00,'Ingreso por venta #189','2026-10-18 11:47:01','Administrador'),(122,36,'POS_CONTADO',720.00,'Ingreso por venta #193','2026-10-19 11:44:19','Administrador'),(123,36,'POS_CONTADO',90.00,'Ingreso por venta #194','2026-10-19 13:17:05','Administrador'),(124,36,'POS_CONTADO',280.00,'Ingreso por venta #195','2026-10-19 13:18:56','Administrador'),(125,36,'POS_CREDITO',120.00,'Ingreso por venta #197','2026-10-19 13:24:43','Administrador');
/*!40000 ALTER TABLE `movimientos_caja` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orden_de_compra`
--

DROP TABLE IF EXISTS `orden_de_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orden_de_compra` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `fecha` date DEFAULT NULL,
  `numero` varchar(25) DEFAULT NULL,
  `proveedor` int DEFAULT NULL,
  `nombre_proveedor` varchar(100) DEFAULT NULL,
  `rnc` varchar(45) DEFAULT NULL,
  `fecha_de_entrega` date DEFAULT NULL,
  `comentario` varchar(255) DEFAULT NULL,
  `moneda` int DEFAULT NULL,
  `subtotal` decimal(12,2) DEFAULT '0.00',
  `total_impuesto` decimal(12,2) DEFAULT '0.00',
  `total_descuento` decimal(12,2) DEFAULT '0.00',
  `total` decimal(12,2) DEFAULT '0.00',
  `anulada` bit(1) DEFAULT b'0',
  `estado` varchar(10) DEFAULT 'PENDIENTE',
  `usuario` int DEFAULT NULL,
  `autorizada` bit(1) DEFAULT b'0',
  `fecha_autorizacion` timestamp NULL DEFAULT NULL,
  `fecha_anulada` timestamp NULL DEFAULT NULL,
  `autorizador` int DEFAULT NULL,
  `nombre_autorizador` varchar(45) DEFAULT NULL,
  `plazo` int DEFAULT NULL,
  `completada` varchar(4) DEFAULT 'NO',
  `total_itbis` decimal(12,2) DEFAULT '0.00',
  `total_a_pagar` decimal(12,2) DEFAULT '0.00',
  `tipo_compra` int DEFAULT NULL,
  `total_isr` decimal(12,2) DEFAULT '0.00',
  `otros_impuesto` decimal(12,2) DEFAULT '0.00',
  `facturada` tinyint(1) NOT NULL DEFAULT '0',
  `fecha_facturada` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `secuencia_documento` int DEFAULT NULL,
  `cotizacion` int DEFAULT NULL,
  `solicitud` int DEFAULT NULL,
  `nombre_solicitante` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_orden_compra_moneda_idx` (`moneda`) USING BTREE,
  KEY `fk_orden_compra_plazo` (`plazo`) USING BTREE,
  KEY `FK_jwp029osbbatkch2lrajci0je` (`tipo_compra`),
  KEY `fk_orden_compra_proveedor_idx` (`proveedor`),
  CONSTRAINT `FK_jwp029osbbatkch2lrajci0je` FOREIGN KEY (`tipo_compra`) REFERENCES `tipo_compra` (`codigo`),
  CONSTRAINT `fk_orden_compra_moneda` FOREIGN KEY (`moneda`) REFERENCES `moneda` (`codigo`),
  CONSTRAINT `fk_orden_compra_plazo` FOREIGN KEY (`plazo`) REFERENCES `plazo` (`codigo`),
  CONSTRAINT `fk_orden_compra_proveedor` FOREIGN KEY (`proveedor`) REFERENCES `proveedor` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orden_de_compra`
--

LOCK TABLES `orden_de_compra` WRITE;
/*!40000 ALTER TABLE `orden_de_compra` DISABLE KEYS */;
INSERT INTO `orden_de_compra` VALUES (10,'2026-10-14','OC-1792005936',10,NULL,NULL,'2026-10-14','dsdfsdfsdfds',NULL,925.00,NULL,NULL,1596.25,_binary '\0','COMPLETADA',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NO',671.25,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL),(11,'2026-10-14','OC-1792007259',9,NULL,NULL,'2027-02-11','qweqwewq',NULL,450.00,NULL,NULL,762.50,_binary '\0','PARCIAL',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NO',312.50,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL),(12,'2026-10-14','OC-1792010515',4,NULL,NULL,'2026-10-14','rwweweew',NULL,15000.00,NULL,NULL,17700.00,_binary '\0','COMPLETADA',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NO',2700.00,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL),(13,'2026-10-14','OC-1792013813',8,'Samuel GrullÃ³n','092-0001750-8','2027-02-11','compra de maercancia a la sid',NULL,23030.00,NULL,NULL,27175.40,_binary '\0','COMPLETADA',NULL,NULL,NULL,NULL,NULL,NULL,7,'SI',4145.40,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL),(18,'2026-10-15','OC-1792070049',4,'Repuestos de Madera Torres EstÃ©vez','n/a','2026-10-15','erwerw',NULL,500.00,NULL,NULL,590.00,_binary '\0','COMPLETADA',NULL,NULL,NULL,NULL,NULL,NULL,9,'SI',90.00,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL),(20,'2026-10-15','OC-1792070808',8,'Samuel GrullÃ³n','092-0001750-8','2027-02-12','ffwerwerewrwe',NULL,1500.00,NULL,NULL,1770.00,_binary '\0','COMPLETADA',NULL,NULL,NULL,NULL,NULL,NULL,7,'SI',270.00,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL),(21,'2026-10-15','OC-1792073023',6,'Repestos Usados NIdIo','8095726258','2026-10-15','werwerwerwe',NULL,7500.00,NULL,NULL,8850.00,_binary '\0','COMPLETADA',NULL,NULL,NULL,NULL,NULL,NULL,10,'SI',1350.00,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL),(22,'2026-10-15','OC-1792073943',5,'REPUESTOS MAO Y/O RAFAEL TRUMAN DOMINGUEZ','03400050799','2026-10-15','qeqeqeqweqw',NULL,15060.00,NULL,NULL,17770.80,_binary '\0','PARCIAL',NULL,NULL,NULL,NULL,NULL,NULL,10,'NO',2710.80,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL),(23,'2026-10-15','OC-1792074275',9,'Clarisol TinÃ©o','04600230702','2027-02-12','rrwerwerwe',NULL,1500.00,NULL,NULL,1770.00,_binary '\0','PARCIAL',NULL,NULL,NULL,NULL,NULL,NULL,7,'NO',270.00,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL),(24,'2026-10-15','OC-1792076857',7,'Comserva, SRL','109011002','2026-12-14','hhbbjbj',NULL,100.00,NULL,NULL,118.00,_binary '\0','COMPLETADA',NULL,NULL,NULL,NULL,NULL,NULL,6,'SI',18.00,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL),(25,'2026-10-15','OC-1792078372',8,'Samuel GrullÃ³n','092-0001750-8','2027-02-12','leche nido',NULL,1575.00,NULL,NULL,1858.50,_binary '\0','COMPLETADA',NULL,NULL,NULL,NULL,NULL,NULL,7,'SI',283.50,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `orden_de_compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permiso`
--

DROP TABLE IF EXISTS `permiso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permiso` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(20) NOT NULL,
  `habilitado` bit(1) NOT NULL DEFAULT b'1',
  `fecha_creacion` datetime NOT NULL,
  `fecha_actualizacion` datetime DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permiso`
--

LOCK TABLES `permiso` WRITE;
/*!40000 ALTER TABLE `permiso` DISABLE KEYS */;
/*!40000 ALTER TABLE `permiso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plazo`
--

DROP TABLE IF EXISTS `plazo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plazo` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(100) DEFAULT NULL,
  `dias` int NOT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plazo`
--

LOCK TABLES `plazo` WRITE;
/*!40000 ALTER TABLE `plazo` DISABLE KEYS */;
INSERT INTO `plazo` VALUES (1,'7 DÃƒÂ­as',7),(2,'10 DÃƒÂ­as',10),(3,'15 DÃƒÂ­as',15),(4,'30 DÃƒÂ­as',30),(5,'45 DÃƒÂ­as',45),(6,'60 DÃƒÂ­as',60),(7,'120 DÃƒÂ­as',120),(8,'1 DÃƒÂ­a',1),(9,'A presentacion',0),(10,'Contado',0),(11,'semanal',3);
/*!40000 ALTER TABLE `plazo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proveedor`
--

DROP TABLE IF EXISTS `proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proveedor` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `rnc` varchar(45) DEFAULT NULL,
  `nombre` varchar(150) DEFAULT NULL,
  `direccion` text,
  `telefono` varchar(45) DEFAULT NULL,
  `email` varchar(45) DEFAULT NULL,
  `contacto` varchar(100) DEFAULT NULL,
  `celular` varchar(45) DEFAULT NULL,
  `fax` varchar(45) DEFAULT NULL,
  `limite_credito` double DEFAULT NULL,
  `plazo` int DEFAULT NULL,
  `cuenta_contable` varchar(45) DEFAULT NULL,
  `web` varchar(50) DEFAULT NULL,
  `tipo_suplidor` int DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `FK9CA22D1C963F1D1` (`plazo`) USING BTREE,
  KEY `fk_suplidor_tipo_suplidor` (`tipo_suplidor`) USING BTREE,
  CONSTRAINT `proveedor_ibfk_3` FOREIGN KEY (`plazo`) REFERENCES `plazo` (`codigo`),
  CONSTRAINT `proveedor_ibfk_4` FOREIGN KEY (`tipo_suplidor`) REFERENCES `tipo_suplidor` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=385 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedor`
--

LOCK TABLES `proveedor` WRITE;
/*!40000 ALTER TABLE `proveedor` DISABLE KEYS */;
INSERT INTO `proveedor` VALUES (3,'03400098236','Maximilino Rosario','santiagoGeneral Luperon No.66 Mao, Valverde','809-563-8745','N/A','Miche Malivert','809-572-4721',NULL,NULL,7,NULL,NULL,2),(4,'n/a','Repuestos de Madera Torres EstÃ©vez','Alto de cana, Santiago RodrÃ­guez, RD','809-994-9963','n/a','','',NULL,NULL,9,NULL,NULL,2),(5,'03400050799','REPUESTOS MAO Y/O RAFAEL TRUMAN DOMINGUEZ','Av. Estanilao Reyes No.24 Mao','8095723240','info@repuestosmao.com','','n/a',NULL,NULL,10,NULL,NULL,1),(6,'8095726258','Repestos Usados NIdIo','J Amaro SÃ¡nchez #102, Mao Valverde, Rep. Dom.','8095726258','N/A','N/A','n/a',NULL,NULL,10,NULL,NULL,2),(7,'109011002','Comserva, SRL','Ave. Estanilao Reyes No. 2, Mao, Rep. Dom.','8095723736','N/A','Comserva','N/A',NULL,NULL,6,NULL,NULL,1),(8,'092-0001750-8','Samuel GrullÃ³n','','','','','',NULL,NULL,7,NULL,NULL,2),(9,'04600230702','Clarisol TinÃ©o','CenovÃ­','','','Miche Malivert-Nicolas EstÃ©vez','',NULL,NULL,7,NULL,NULL,2),(10,'04600156642','TeÃ³filo Antonio RodrÃ­guez','','','','','04600156642',NULL,NULL,9,NULL,NULL,2),(11,'101001577','CLARO','','809-220-1111','','','',NULL,NULL,5,NULL,NULL,1);
/*!40000 ALTER TABLE `proveedor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recepcion_mercancia`
--

DROP TABLE IF EXISTS `recepcion_mercancia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recepcion_mercancia` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL,
  `tipo_documento` int NOT NULL,
  `numero_documento` varchar(50) NOT NULL,
  `nombre_documento` varchar(50) NOT NULL,
  `proveedor` int NOT NULL,
  `nombre_proveedor` varchar(150) DEFAULT NULL,
  `comentario` text NOT NULL,
  `subtotal` decimal(12,2) NOT NULL DEFAULT '0.00',
  `total_itbis` decimal(12,2) NOT NULL DEFAULT '0.00',
  `total` decimal(12,2) NOT NULL DEFAULT '0.00',
  `estado_documento` int NOT NULL,
  `nombre_estado` varchar(20) NOT NULL,
  `usuario` int NOT NULL,
  `nombre_usuario` varchar(120) NOT NULL,
  `almacen` int DEFAULT NULL,
  `orden_compra` int DEFAULT NULL,
  `numero_oc` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_recepcion_proveedor_idx` (`proveedor`),
  KEY `fk_recepcion_tipo_documento_idx` (`tipo_documento`),
  KEY `fk_recepcion_estado_idx` (`estado_documento`),
  KEY `fk_recepcion_usuario_idx` (`usuario`),
  KEY `fk_recepcion_alm_idx` (`almacen`),
  KEY `fk_recepcion_oc_idx` (`orden_compra`),
  CONSTRAINT `fk_recepcion_alm` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_recepcion_estado` FOREIGN KEY (`estado_documento`) REFERENCES `estado_documento` (`codigo`),
  CONSTRAINT `fk_recepcion_oc` FOREIGN KEY (`orden_compra`) REFERENCES `orden_de_compra` (`codigo`),
  CONSTRAINT `fk_recepcion_proveedor` FOREIGN KEY (`proveedor`) REFERENCES `proveedor` (`codigo`),
  CONSTRAINT `fk_recepcion_tipo_documento` FOREIGN KEY (`tipo_documento`) REFERENCES `tipo_documento` (`codigo`),
  CONSTRAINT `fk_recepcion_usuario` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recepcion_mercancia`
--

LOCK TABLES `recepcion_mercancia` WRITE;
/*!40000 ALTER TABLE `recepcion_mercancia` DISABLE KEYS */;
INSERT INTO `recepcion_mercancia` VALUES (53,'2026-10-13 00:00:00',1,'REC-1791917422','321',3,'Maximilino Rosario','entrada',1500.00,270.00,1770.00,2,'Recibido Parcial',1,'Admin',1,NULL,NULL),(54,'2026-10-13 00:00:00',1,'REC-1791919187','5241',5,'REPUESTOS MAO Y/O RAFAEL TRUMAN DOMINGUEZ','entrada',500.00,90.00,590.00,2,'Recibido Parcial',1,'Admin',2,NULL,NULL),(55,'2026-10-13 00:00:00',9,'REC-1791924873','142',7,'Comserva, SRL','entrada de mercanica',1200.00,216.00,1416.00,2,'Recibido Parcial',1,'Admin',2,NULL,NULL),(56,'2026-10-14 00:00:00',9,'REC-1791986592','23423432',7,'Comserva, SRL','entrada de mercancia',100.00,18.00,118.00,2,'Recibido Parcial',1,'Admin',1,NULL,NULL),(59,'2026-10-14 00:00:00',8,'REC-1792009755','ORDEN COMPRA',9,'Clarisol TinÃ©o','dsadasdasd',50.00,25.00,75.00,2,'Recibido Parcial',1,'Admin',1,NULL,NULL),(60,'2026-10-14 00:00:00',8,'REC-1792009876','ORDEN COMPRA',10,'TeÃ³filo Antonio RodrÃ­guez','erwerwerwerewr',925.00,671.25,1596.25,2,'Recibido Parcial',1,'Admin',1,NULL,NULL),(62,'2026-10-14 00:00:00',8,'REC-1792010538','ORDEN COMPRA',4,'Repuestos de Madera Torres EstÃ©vez','eerwerwerwewerwerwer',15000.00,2700.00,17700.00,2,'Recibido Parcial',1,'Admin',1,NULL,NULL),(63,'2026-10-14 00:00:00',8,'REC-1792012178','ORDEN COMPRA',4,'Repuestos de Madera Torres EstÃ©vez','PROBANDO OC',15000.00,2700.00,17700.00,2,'Recibido Parcial',1,'Admin',1,12,'12'),(66,'2026-10-14 00:00:00',8,'REC-1792013312','ORDEN COMPRA',4,'Repuestos de Madera Torres EstÃ©vez','wrwerwrew',15000.00,2700.00,17700.00,2,'Recibido Parcial',1,'Admin',1,12,'12'),(67,'2026-10-14 00:00:00',8,'REC-1792013331','ORDEN COMPRA',10,'TeÃ³filo Antonio RodrÃ­guez','werwerew',925.00,671.25,1596.25,2,'Recibido Parcial',1,'Admin',1,10,'10'),(83,'2026-10-15 00:00:00',8,'REC-1792072320','ORDEN COMPRA',8,'Samuel GrullÃ³n','werwerwe',1500.00,270.00,1770.00,2,'Recibido Parcial',1,'Admin',1,20,'20'),(84,'2026-10-15 00:00:00',8,'REC-1792072577','ORDEN COMPRA',4,'Repuestos de Madera Torres EstÃ©vez','werwrwerwe',500.00,90.00,590.00,2,'Recibido Parcial',1,'Admin',1,18,'18'),(86,'2026-10-15 00:00:00',8,'REC-1792072836','ORDEN COMPRA',9,'Clarisol TinÃ©o','wrwrwer',300.00,87.50,387.50,2,'Recibido Parcial',1,'Admin',1,11,'11'),(87,'2026-10-15 00:00:00',8,'REC-1792073600','ORDEN COMPRA',8,'Samuel GrullÃ³n','rewrwerwer',23030.00,4145.40,27175.40,2,'Recibido Parcial',1,'Admin',1,13,'13'),(88,'2026-10-15 00:00:00',8,'REC-1792073613','ORDEN COMPRA',6,'Repestos Usados NIdIo','dwqeqweqwew',7500.00,1350.00,8850.00,2,'Recibido Parcial',1,'Admin',1,21,'21'),(89,'2026-10-15 00:00:00',9,'6325','RECEPCION DE MERCANCIA',6,'Repestos Usados NIdIo','werwerew',1200.00,216.00,1416.00,2,'Recibido Parcial',1,'Admin',1,NULL,NULL),(90,'2026-10-15 00:00:00',8,'REC-1792073973','ORDEN COMPRA',5,'REPUESTOS MAO Y/O RAFAEL TRUMAN DOMINGUEZ','mecancia incompleta',7560.00,1360.80,8920.80,2,'Recibido Parcial',1,'Admin',1,22,'22'),(91,'2026-10-15 00:00:00',8,'REC-1792074298','ORDEN COMPRA',9,'Clarisol TinÃ©o','se recibio una caja de leche de tres que se pideiron',500.00,90.00,590.00,2,'Recibido Parcial',1,'Admin',1,23,'23'),(92,'2026-10-15 00:00:00',8,'REC-1792076952','ORDEN COMPRA',7,'Comserva, SRL','eqweqwe',100.00,18.00,118.00,2,'Recibido Parcial',1,'Admin',1,24,'24'),(93,'2026-10-15 00:00:00',8,'REC-1792078406','ORDEN COMPRA',8,'Samuel GrullÃ³n','entrada de leche nido',1575.00,283.50,1858.50,2,'Recibido Parcial',1,'Admin',1,25,'25'),(94,'2026-10-19 00:00:00',9,'20261019','RECEPCION DE MERCANCIA',8,'Samuel GrullÃ³n','entrada de platano',750.00,135.00,885.00,2,'Recibido Parcial',1,'Admin',1,NULL,NULL);
/*!40000 ALTER TABLE `recepcion_mercancia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) NOT NULL,
  `fecha` date NOT NULL,
  `descripcion` text,
  `fecha_creacion` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `creado_por` varchar(0) DEFAULT NULL,
  `usuario` int NOT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_rol_usuario_idx` (`usuario`) USING BTREE,
  CONSTRAINT `rol_ibfk_1` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES (1,'ADMINISTRADOR','2020-03-04','ADMINISTRA TODO EL SISTEMA',NULL,NULL,1),(2,'AUXILIAR','2020-08-14','PARA AUXILIAR',NULL,NULL,1),(3,'ASISTENTE ADMINISTRATIVO','2021-06-24','ROL PARA  GESTIONAR  LOS PERMISOS ADMINISTRATIVOS',NULL,NULL,1),(4,'AUX CONTABILIDAD','2021-06-28','GESTIUONAR PERMISOS DE CONTABILIODAD',NULL,NULL,1),(5,'EJECUTIVO DE VENTA','2021-11-18','ESTE ROL ES PARA EJECUTIVO DE VENTA DE IGHTRACK\nSOLAMENTE',NULL,NULL,1),(6,'CONTABILIDAD','2022-12-13','ROL PARA GESTIONAR LA PARTE ADMINISTRATIVA',NULL,NULL,1),(7,'VENTAS  /ASISTENTE ADMINISTRATIVO','2023-07-20','VENTAS Y ASISTENTE ADMINISTRATIVO',NULL,NULL,1);
/*!40000 ALTER TABLE `rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol_permiso`
--

DROP TABLE IF EXISTS `rol_permiso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol_permiso` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `rol` int NOT NULL,
  `permiso` int NOT NULL,
  `nombre_rol` varchar(20) NOT NULL,
  `nombre_permiso` varchar(20) NOT NULL,
  `habilitado` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`codigo`),
  KEY `fk_rol_permiso_rol` (`rol`),
  CONSTRAINT `fk_rol_permiso_rol` FOREIGN KEY (`rol`) REFERENCES `rol` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol_permiso`
--

LOCK TABLES `rol_permiso` WRITE;
/*!40000 ALTER TABLE `rol_permiso` DISABLE KEYS */;
/*!40000 ALTER TABLE `rol_permiso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `salida_inventario`
--

DROP TABLE IF EXISTS `salida_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `salida_inventario` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `numero` int DEFAULT NULL,
  `secuencia_documento` int DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `solicitud_salida` int DEFAULT NULL,
  `fecha_registro` timestamp NULL DEFAULT NULL,
  `observacion` varchar(150) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `fecha_contabilizacion` timestamp NULL DEFAULT NULL,
  `tipo_documento` int DEFAULT NULL,
  `numero_documento` varchar(45) DEFAULT NULL,
  `moneda` int DEFAULT NULL,
  `solicitante` varchar(80) DEFAULT NULL,
  `anulada` tinyint(1) NOT NULL,
  `fecha_anulada` timestamp NULL DEFAULT NULL,
  `usuario` int DEFAULT NULL,
  `anulada_por` int DEFAULT NULL,
  `nombre_usuario` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_salida_inv` (`secuencia_documento`),
  KEY `salida_inventario_ibfk_1` (`usuario`),
  KEY `fk_salida_inv_tipo_doc_idx` (`tipo_documento`),
  CONSTRAINT `fk_salida_inv` FOREIGN KEY (`secuencia_documento`) REFERENCES `secuencia_documento` (`codigo`),
  CONSTRAINT `fk_salida_inv_tipo_doc` FOREIGN KEY (`tipo_documento`) REFERENCES `tipo_documento` (`codigo`),
  CONSTRAINT `fk_salida_inv_usaurio` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`),
  CONSTRAINT `salida_inventario_ibfk_1` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=296 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `salida_inventario`
--

LOCK TABLES `salida_inventario` WRITE;
/*!40000 ALTER TABLE `salida_inventario` DISABLE KEYS */;
INSERT INTO `salida_inventario` VALUES (224,NULL,NULL,'2026-10-14',NULL,'2026-10-14 14:01:02','Salida automÃ¡tica por Factura POS #142','2026-10-14 14:01:02',5,'142',NULL,NULL,0,NULL,1,NULL,'Administrador'),(225,NULL,NULL,'2026-10-14',NULL,'2026-10-14 14:04:46','Salida por Traslado de Inventario #71. ',NULL,2,'71',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(226,NULL,NULL,'2026-10-14',NULL,'2026-10-14 14:05:28','Salida automÃ¡tica por Factura POS #143','2026-10-14 14:05:28',5,'143',NULL,NULL,0,NULL,1,NULL,'Administrador'),(227,NULL,NULL,'2026-10-14',NULL,'2026-10-14 14:09:53','Salida automÃ¡tica por Factura POS #144','2026-10-14 14:09:53',5,'144',NULL,NULL,0,NULL,1,NULL,'Administrador'),(228,NULL,NULL,'2026-10-14',NULL,'2026-10-14 14:18:43','Salida automÃ¡tica por Factura POS #145','2026-10-14 14:18:43',5,'145',NULL,NULL,0,NULL,1,NULL,'Administrador'),(229,NULL,NULL,'2026-10-14',NULL,'2026-10-14 14:20:07','Salida automÃ¡tica por Factura POS #146','2026-10-14 14:20:07',5,'146',NULL,NULL,0,NULL,1,NULL,'Administrador'),(230,NULL,NULL,'2026-10-14',NULL,'2026-10-14 14:26:17','Salida automÃ¡tica por Factura POS #147','2026-10-14 14:26:17',5,'147',NULL,NULL,0,NULL,1,NULL,'Administrador'),(231,NULL,NULL,'2026-10-14',NULL,'2026-10-14 14:26:58','Salida automÃ¡tica por Factura POS #148','2026-10-14 14:26:58',5,'148',NULL,NULL,0,NULL,1,NULL,'Administrador'),(232,NULL,NULL,'2026-10-14',NULL,'2026-10-14 14:33:24','Salida automÃ¡tica por Factura POS #149','2026-10-14 14:33:24',5,'149',NULL,NULL,0,NULL,1,NULL,'Administrador'),(233,NULL,NULL,'2026-10-14',NULL,'2026-10-14 14:48:31','Salida automÃ¡tica por Factura POS #150','2026-10-14 14:48:31',5,'150',NULL,NULL,0,NULL,1,NULL,'Administrador'),(235,NULL,NULL,'2026-10-14',NULL,'2026-10-14 15:36:03','Salida por Ajuste de Inventario #87. ajuste',NULL,3,'87',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(236,NULL,NULL,'2026-10-14',NULL,'2026-10-14 15:38:45','Salida por Traslado de Inventario #72. ',NULL,2,'72',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(237,NULL,NULL,'2026-10-14',NULL,'2026-10-14 15:48:13','Salida automÃ¡tica por Factura POS #152','2026-10-14 15:48:13',5,'152',NULL,NULL,0,NULL,1,NULL,'Administrador'),(238,NULL,NULL,'2026-10-14',NULL,'2026-10-14 17:35:03','Salida automÃ¡tica por Factura POS #153','2026-10-14 17:35:03',5,'153',NULL,NULL,0,NULL,1,NULL,'Administrador'),(239,NULL,NULL,'2026-10-14',NULL,'2026-10-14 17:36:48','Salida por Traslado de Inventario #73. ',NULL,2,'73',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(240,NULL,NULL,'2026-10-14',NULL,'2026-10-14 17:37:30','Salida automÃ¡tica por Factura POS #154','2026-10-14 17:37:30',5,'154',NULL,NULL,0,NULL,1,NULL,'Administrador'),(241,NULL,NULL,'2026-10-14',NULL,'2026-10-14 17:54:30','Salida automÃ¡tica por Factura POS #155','2026-10-14 17:54:30',5,'155',NULL,NULL,0,NULL,1,NULL,'Administrador'),(242,NULL,NULL,'2026-10-14',NULL,'2026-10-14 17:57:58','Salida automÃ¡tica por Factura POS #156','2026-10-14 17:57:58',5,'156',NULL,NULL,0,NULL,1,NULL,'Administrador'),(243,NULL,NULL,'2026-10-14',NULL,'2026-10-14 18:01:01','Salida por Traslado de Inventario #74. ',NULL,2,'74',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(244,NULL,NULL,'2026-10-14',NULL,'2026-10-14 18:01:49','Salida automÃ¡tica por Factura POS #157','2026-10-14 18:01:49',5,'157',NULL,NULL,0,NULL,1,NULL,'Administrador'),(245,NULL,NULL,'2026-10-14',NULL,'2026-10-14 18:07:38','Salida automÃ¡tica por Factura POS #158','2026-10-14 18:07:38',5,'158',NULL,NULL,0,NULL,1,NULL,'Administrador'),(246,NULL,NULL,'2026-10-15',NULL,'2026-10-15 15:19:27','Salida automÃ¡tica por Factura POS #159','2026-10-15 15:19:27',5,'159',NULL,NULL,0,NULL,1,NULL,'Administrador'),(247,NULL,NULL,'2026-10-15',NULL,'2026-10-15 15:27:41','Salida por Traslado de Inventario #75. ',NULL,2,'75',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(248,NULL,NULL,'2026-10-15',NULL,'2026-10-15 15:34:24','Salida por Traslado de Inventario #76. ',NULL,2,'76',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(249,NULL,NULL,'2026-10-15',NULL,'2026-10-15 15:35:38','Salida automÃ¡tica por Factura POS #160','2026-10-15 15:35:38',5,'160',NULL,NULL,0,NULL,1,NULL,'Administrador'),(251,NULL,NULL,'2026-10-15',NULL,'2026-10-15 17:12:19','Salida automÃ¡tica por Factura POS #162','2026-10-15 17:12:19',5,'162',NULL,NULL,0,NULL,1,NULL,'Administrador'),(253,NULL,NULL,'2026-10-15',NULL,'2026-10-15 17:52:10','Salida por Traslado de Inventario #77. ',NULL,2,'77',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(254,NULL,NULL,'2026-10-15',NULL,'2026-10-15 17:52:26','Salida automÃ¡tica por Factura POS #164','2026-10-15 17:52:26',5,'164',NULL,NULL,0,NULL,1,NULL,'Administrador'),(256,NULL,NULL,'2026-10-15',NULL,'2026-10-15 19:01:59','Salida automÃ¡tica por Factura POS #166','2026-10-15 19:01:59',5,'166',NULL,NULL,0,NULL,1,NULL,'Administrador'),(258,NULL,NULL,'2026-10-15',NULL,'2026-10-15 19:11:49','Salida automÃ¡tica por Factura POS #168','2026-10-15 19:11:49',5,'168',NULL,NULL,0,NULL,1,NULL,'Administrador'),(259,NULL,NULL,'2026-10-16',NULL,'2026-10-16 15:34:17','Salida automÃ¡tica por Factura POS #169','2026-10-16 15:34:17',5,'169',NULL,NULL,0,NULL,1,NULL,'Administrador'),(260,NULL,NULL,'2026-10-16',NULL,'2026-10-16 15:36:32','Salida por Traslado de Inventario #78. ',NULL,2,'78',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(261,NULL,NULL,'2026-10-16',NULL,'2026-10-16 15:36:51','Salida automÃ¡tica por Factura POS #170','2026-10-16 15:36:51',5,'170',NULL,NULL,0,NULL,1,NULL,'Administrador'),(263,NULL,NULL,'2026-10-16',NULL,'2026-10-16 15:38:09','Salida por Traslado de Inventario #79. ',NULL,2,'79',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(264,NULL,NULL,'2026-10-16',NULL,'2026-10-16 15:38:33','Salida automÃ¡tica por Factura POS #172','2026-10-16 15:38:33',5,'172',NULL,NULL,0,NULL,1,NULL,'Administrador'),(265,NULL,NULL,'2026-10-16',NULL,'2026-10-16 15:42:08','Salida por Ajuste de Inventario #90. DISMINUCION DE 6 UN',NULL,3,'90',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(266,NULL,NULL,'2026-10-16',NULL,'2026-10-16 15:43:24','Salida automÃ¡tica por Factura POS #173','2026-10-16 15:43:24',5,'173',NULL,NULL,0,NULL,1,NULL,'Administrador'),(267,NULL,NULL,'2026-10-16',NULL,'2026-10-16 15:44:55','Salida automÃ¡tica por Factura POS #174','2026-10-16 15:44:55',5,'174',NULL,NULL,0,NULL,1,NULL,'Administrador'),(268,NULL,NULL,'2026-10-16',NULL,'2026-10-16 15:46:57','Salida automÃ¡tica por Factura POS #175','2026-10-16 15:46:57',5,'175',NULL,NULL,0,NULL,1,NULL,'Administrador'),(269,NULL,NULL,'2026-10-17',NULL,'2026-10-17 15:25:55','Salida por Traslado de Inventario #80. ',NULL,2,'80',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(270,NULL,NULL,'2026-10-17',NULL,'2026-10-17 15:31:06','Salida automÃ¡tica por Factura POS #176','2026-10-17 15:31:06',5,'176',NULL,NULL,0,NULL,1,NULL,'Administrador'),(273,NULL,NULL,'2026-10-17',NULL,'2026-10-17 15:37:28','Salida automÃ¡tica por Factura POS #179','2026-10-17 15:37:28',5,'179',NULL,NULL,0,NULL,1,NULL,'Administrador'),(275,NULL,NULL,'2026-10-17',NULL,'2026-10-17 16:36:41','Salida automÃ¡tica por Factura POS #181','2026-10-17 16:36:41',5,'181',NULL,NULL,0,NULL,1,NULL,'Administrador'),(277,NULL,NULL,'2026-10-17',NULL,'2026-10-17 16:41:10','Salida automÃ¡tica por Factura POS #183','2026-10-17 16:41:10',5,'183',NULL,NULL,0,NULL,1,NULL,'Administrador'),(279,NULL,NULL,'2026-10-17',NULL,'2026-10-17 20:25:49','Salida automÃ¡tica por Factura POS #185','2026-10-17 20:25:49',5,'185',NULL,NULL,0,NULL,1,NULL,'Administrador'),(281,NULL,NULL,'2026-10-18',NULL,'2026-10-18 15:43:10','Salida automÃ¡tica por Factura POS #187','2026-10-18 15:43:10',5,'187',NULL,NULL,0,NULL,1,NULL,'Administrador'),(283,NULL,NULL,'2026-10-18',NULL,'2026-10-18 15:47:01','Salida automÃ¡tica por Factura POS #189','2026-10-18 15:47:01',5,'189',NULL,NULL,0,NULL,1,NULL,'Administrador'),(287,NULL,NULL,'2026-10-19',NULL,'2026-10-19 15:44:19','Salida automÃ¡tica por Factura POS #193','2026-10-19 15:44:19',5,'193',NULL,NULL,0,NULL,1,NULL,'Administrador'),(288,NULL,NULL,'2026-10-19',NULL,'2026-10-19 17:16:34','Salida por Traslado de Inventario #81. ',NULL,2,'81',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(289,NULL,NULL,'2026-10-19',NULL,'2026-10-19 17:17:05','Salida automÃ¡tica por Factura POS #194','2026-10-19 17:17:05',5,'194',NULL,NULL,0,NULL,1,NULL,'Administrador'),(290,NULL,NULL,'2026-10-19',NULL,'2026-10-19 17:18:56','Salida automÃ¡tica por Factura POS #195','2026-10-19 17:18:56',5,'195',NULL,NULL,0,NULL,1,NULL,'Administrador'),(292,NULL,NULL,'2026-10-19',NULL,'2026-10-19 17:24:07','Salida por Traslado de Inventario #82. ',NULL,2,'82',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(293,NULL,NULL,'2026-10-19',NULL,'2026-10-19 17:24:43','Salida automÃ¡tica por Factura POS #197','2026-10-19 17:24:43',5,'197',NULL,NULL,0,NULL,1,NULL,'Administrador'),(294,NULL,NULL,'2026-10-19',NULL,'2026-10-19 17:27:28','Salida por Ajuste de Inventario #91. POR MERMA',NULL,3,'91',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(295,NULL,NULL,'2026-10-19',NULL,'2026-10-19 17:57:50','Salida por Merma de Inventario #4. se pudieron',NULL,2,'4',NULL,NULL,0,NULL,1,NULL,'SISTEMA');
/*!40000 ALTER TABLE `salida_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `secuencia_documento`
--

DROP TABLE IF EXISTS `secuencia_documento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `secuencia_documento` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `tipo_documento` int NOT NULL,
  `numero` int NOT NULL,
  `prefijo` varchar(4) DEFAULT NULL,
  `usuario` int NOT NULL,
  `sufijo` varchar(10) DEFAULT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP,
  `creado_por` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_secuencia_doc_tipo_documento` (`tipo_documento`),
  CONSTRAINT `fk_sec_documento_tipo_doc` FOREIGN KEY (`tipo_documento`) REFERENCES `tipo_documento` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `secuencia_documento`
--

LOCK TABLES `secuencia_documento` WRITE;
/*!40000 ALTER TABLE `secuencia_documento` DISABLE KEYS */;
/*!40000 ALTER TABLE `secuencia_documento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sub_categoria`
--

DROP TABLE IF EXISTS `sub_categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sub_categoria` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `categoria` int NOT NULL,
  `nombre` varchar(45) NOT NULL,
  `fecha_creacion` timestamp NOT NULL,
  `creada_por` varchar(50) NOT NULL,
  `habilitada` bit(1) NOT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_sub_categoria_categoria` (`categoria`) USING BTREE,
  CONSTRAINT `sub_categoria_ibfk_1` FOREIGN KEY (`categoria`) REFERENCES `categoria` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sub_categoria`
--

LOCK TABLES `sub_categoria` WRITE;
/*!40000 ALTER TABLE `sub_categoria` DISABLE KEYS */;
/*!40000 ALTER TABLE `sub_categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tipo_ajuste`
--

DROP TABLE IF EXISTS `tipo_ajuste`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_ajuste` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_ajuste`
--

LOCK TABLES `tipo_ajuste` WRITE;
/*!40000 ALTER TABLE `tipo_ajuste` DISABLE KEYS */;
INSERT INTO `tipo_ajuste` VALUES (1,'INCREMENTO'),(2,'DISMINUCION');
/*!40000 ALTER TABLE `tipo_ajuste` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tipo_compra`
--

DROP TABLE IF EXISTS `tipo_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_compra` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_compra`
--

LOCK TABLES `tipo_compra` WRITE;
/*!40000 ALTER TABLE `tipo_compra` DISABLE KEYS */;
/*!40000 ALTER TABLE `tipo_compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tipo_documento`
--

DROP TABLE IF EXISTS `tipo_documento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_documento` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) NOT NULL,
  `modulo` int DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `creado_por` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_documento`
--

LOCK TABLES `tipo_documento` WRITE;
/*!40000 ALTER TABLE `tipo_documento` DISABLE KEYS */;
INSERT INTO `tipo_documento` VALUES (1,'ENTRADA DE INVENTARIO',1,'2026-09-16 04:00:00','ADMIN'),(2,'SALIDA DE INVENTARIO',1,'2026-09-16 04:00:00','ADMIN'),(3,'AJUSTE DE INVENTARIO',1,'2026-09-16 04:00:00','ADMIN'),(4,'TRANSFERENCIA DE INVENTARIO',1,'2026-09-18 04:00:00','ADMIN'),(5,'FACTURA DE VENTA',1,'2026-09-18 04:00:00','ADMIN'),(6,'ANULACION  DE  FACTURA',1,'2026-09-22 19:13:09','ADMIN'),(7,'DEVOLUCION DE VENTA',1,'2026-09-22 04:00:00','ADMIN'),(8,'ORDEN COMPRA',1,'2026-09-29 04:00:00','ADMIN'),(9,'RECEPCION DE MERCANCIA',1,'2026-10-02 04:00:00','ADMIN');
/*!40000 ALTER TABLE `tipo_documento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tipo_merma`
--

DROP TABLE IF EXISTS `tipo_merma`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_merma` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(100) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`codigo`),
  UNIQUE KEY `uk_tipo_merma_descripcion` (`descripcion`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_merma`
--

LOCK TABLES `tipo_merma` WRITE;
/*!40000 ALTER TABLE `tipo_merma` DISABLE KEYS */;
INSERT INTO `tipo_merma` VALUES (1,'VENCIMIENTO',1),(2,'DAÃ‘O / ROTURA',1),(3,'ROBO / PÃ‰RDIDA',1),(4,'CONSUMO INTERNO',1),(5,'OTRO',1);
/*!40000 ALTER TABLE `tipo_merma` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tipo_movimiento`
--

DROP TABLE IF EXISTS `tipo_movimiento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_movimiento` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) NOT NULL,
  `fecha_creacion` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `creado_por` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_movimiento`
--

LOCK TABLES `tipo_movimiento` WRITE;
/*!40000 ALTER TABLE `tipo_movimiento` DISABLE KEYS */;
INSERT INTO `tipo_movimiento` VALUES (1,'ENTRADA','2026-09-15 04:00:00','ADMIN'),(2,'SALIDA','2026-09-15 04:00:00','ADMIN'),(3,'AJUSTE','2026-09-15 04:00:00','ADMIN');
/*!40000 ALTER TABLE `tipo_movimiento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tipo_suplidor`
--

DROP TABLE IF EXISTS `tipo_suplidor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_suplidor` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(20) NOT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_suplidor`
--

LOCK TABLES `tipo_suplidor` WRITE;
/*!40000 ALTER TABLE `tipo_suplidor` DISABLE KEYS */;
INSERT INTO `tipo_suplidor` VALUES (1,'Formal'),(2,'Informal');
/*!40000 ALTER TABLE `tipo_suplidor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tipo_venta`
--

DROP TABLE IF EXISTS `tipo_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_venta` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_venta`
--

LOCK TABLES `tipo_venta` WRITE;
/*!40000 ALTER TABLE `tipo_venta` DISABLE KEYS */;
INSERT INTO `tipo_venta` VALUES (1,'CONTADO'),(2,'CREDITO');
/*!40000 ALTER TABLE `tipo_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `traslado_inventario`
--

DROP TABLE IF EXISTS `traslado_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `traslado_inventario` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `numero_documento` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_emision` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_recepcion` datetime DEFAULT NULL,
  `almacen_origen` int NOT NULL,
  `almacen_destino` int NOT NULL,
  `usuario_envia` int NOT NULL,
  `usuario_recibe` int DEFAULT NULL,
  `estado` int NOT NULL,
  `observacion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  UNIQUE KEY `uk_numero_documento_traslado` (`numero_documento`),
  KEY `fk_traslado_almacen_destino` (`almacen_destino`),
  KEY `fk_traslado_almacen_origen` (`almacen_origen`),
  KEY `fk_traslado_usuario_envia` (`usuario_envia`),
  KEY `fk_traslado_usuario_recibe` (`usuario_recibe`),
  KEY `fk_traslado_almacen_estado_idx` (`estado`),
  CONSTRAINT `fk_traslado_almacen_destino` FOREIGN KEY (`almacen_destino`) REFERENCES `almacen` (`codigo`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_traslado_almacen_estado` FOREIGN KEY (`estado`) REFERENCES `estado_documento` (`codigo`),
  CONSTRAINT `fk_traslado_almacen_origen` FOREIGN KEY (`almacen_origen`) REFERENCES `almacen` (`codigo`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_traslado_usuario_envia` FOREIGN KEY (`usuario_envia`) REFERENCES `usuario` (`codigo`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_traslado_usuario_recibe` FOREIGN KEY (`usuario_recibe`) REFERENCES `usuario` (`codigo`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=83 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `traslado_inventario`
--

LOCK TABLES `traslado_inventario` WRITE;
/*!40000 ALTER TABLE `traslado_inventario` DISABLE KEYS */;
INSERT INTO `traslado_inventario` VALUES (67,'TR-1791918771620','2026-10-13 15:12:52',NULL,1,2,1,NULL,3,''),(68,'TR-1791924670449','2026-10-13 16:51:10',NULL,1,2,1,NULL,3,''),(69,'TR-1791924710969','2026-10-13 16:51:51',NULL,2,1,1,NULL,3,''),(70,'TR-1791983540963','2026-10-14 09:12:21',NULL,1,2,1,NULL,3,''),(71,'TR-1791986685462','2026-10-14 10:04:45',NULL,1,2,1,NULL,3,''),(72,'TR-1791992325231','2026-10-14 11:38:45',NULL,1,2,1,NULL,3,''),(73,'TR-1791999408315','2026-10-14 13:36:48',NULL,1,2,1,NULL,3,''),(74,'TR-1792000861186','2026-10-14 14:01:01',NULL,1,2,1,NULL,3,''),(75,'TR-1792078061392','2026-10-15 11:27:41',NULL,1,2,1,NULL,3,''),(76,'TR-1792078463649','2026-10-15 11:34:24',NULL,1,2,1,NULL,3,''),(77,'TR-1792086729775','2026-10-15 13:52:10',NULL,1,2,1,NULL,3,''),(78,'TR-1792164991566','2026-10-16 11:36:32',NULL,1,2,1,NULL,3,''),(79,'TR-1792165088564','2026-10-16 11:38:09',NULL,1,2,1,NULL,3,''),(80,'TR-1792250755054','2026-10-17 11:25:55',NULL,1,2,1,NULL,3,''),(81,'TR-1792430194107','2026-10-19 13:16:34',NULL,1,2,1,NULL,3,''),(82,'TR-1792430646843','2026-10-19 13:24:07',NULL,1,2,1,NULL,3,'');
/*!40000 ALTER TABLE `traslado_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `unidad`
--

DROP TABLE IF EXISTS `unidad`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `unidad` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(50) DEFAULT NULL,
  `abreviatura` varchar(6) DEFAULT NULL,
  `creada_por` varchar(50) DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `habilitada` bit(1) NOT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unidad`
--

LOCK TABLES `unidad` WRITE;
/*!40000 ALTER TABLE `unidad` DISABLE KEYS */;
INSERT INTO `unidad` VALUES (1,'UNIDAD','UD','ADMIN','2025-09-26 18:30:00',_binary ''),(2,'PAQUETE','PQ','ADMIN','2026-10-05 13:05:51',_binary ''),(3,'CAJA','CJ','ADMIN','2026-10-05 13:05:51',_binary ''),(4,'LIBRA','LB','ADMIN','2026-10-05 13:07:18',_binary ''),(5,'SACO','SC','ADMIN','2026-10-05 13:07:18',_binary ''),(6,'SOBRE','SB','ADMIN','2026-10-05 13:13:48',_binary ''),(7,'FALDO','FD','ADMIN','2026-10-05 13:13:48',_binary ''),(8,'CAJITA','CJ','ADMIN','2026-10-05 13:13:48',_binary '');
/*!40000 ALTER TABLE `unidad` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `unidad_de_venta`
--

DROP TABLE IF EXISTS `unidad_de_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `unidad_de_venta` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(15) NOT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unidad_de_venta`
--

LOCK TABLES `unidad_de_venta` WRITE;
/*!40000 ALTER TABLE `unidad_de_venta` DISABLE KEYS */;
INSERT INTO `unidad_de_venta` VALUES (1,'UNIDAD'),(2,'LIBRA'),(3,'PAQUETE');
/*!40000 ALTER TABLE `unidad_de_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) DEFAULT NULL,
  `tipo_usuario` varchar(100) DEFAULT NULL,
  `usuario` varchar(25) DEFAULT NULL,
  `contrasena` varchar(80) DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `creado_por` varchar(50) DEFAULT NULL,
  `habilitado` bit(1) NOT NULL DEFAULT b'1',
  `fecha_actualizacion` datetime DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'admin','1','admin','123','2026-09-15 04:00:00','admin',_binary '',NULL);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_rol`
--

DROP TABLE IF EXISTS `usuario_rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_rol` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `usuario` int NOT NULL,
  `rol` int NOT NULL,
  `nombre_rol` varchar(20) NOT NULL,
  `nombre_usuario` varchar(30) NOT NULL,
  `habilitado` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`codigo`),
  KEY `fk_usuario_rol_us` (`usuario`),
  CONSTRAINT `fk_usuario_rol_us` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_rol`
--

LOCK TABLES `usuario_rol` WRITE;
/*!40000 ALTER TABLE `usuario_rol` DISABLE KEYS */;
/*!40000 ALTER TABLE `usuario_rol` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-19 14:02:35
