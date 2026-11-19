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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ajuste_inventario`
--

LOCK TABLES `ajuste_inventario` WRITE;
/*!40000 ALTER TABLE `ajuste_inventario` DISABLE KEYS */;
INSERT INTO `ajuste_inventario` VALUES (1,'2026-11-17','2026-11-17 10:13:53',1,1,'aumento',0,'ADMIN',NULL,1,'ALMCEN  PRINCIPAL');
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
-- Table structure for table `aplicacion_credito_proveedor`
--

DROP TABLE IF EXISTS `aplicacion_credito_proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `aplicacion_credito_proveedor` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `devolucion` int NOT NULL,
  `factura_compra` int NOT NULL,
  `recibo_egreso` int NOT NULL,
  `monto` decimal(14,2) NOT NULL,
  `fecha` datetime NOT NULL,
  `usuario` varchar(60) DEFAULT NULL,
  `anulada` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`codigo`),
  KEY `idx_aplcred_devolucion` (`devolucion`),
  KEY `fk_aplcred_factura` (`factura_compra`),
  KEY `fk_aplcred_recibo` (`recibo_egreso`),
  CONSTRAINT `fk_aplcred_devolucion` FOREIGN KEY (`devolucion`) REFERENCES `devolucion_proveedor` (`codigo`),
  CONSTRAINT `fk_aplcred_factura` FOREIGN KEY (`factura_compra`) REFERENCES `factura_de_compra` (`codigo`),
  CONSTRAINT `fk_aplcred_recibo` FOREIGN KEY (`recibo_egreso`) REFERENCES `recibo_de_egreso` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `aplicacion_credito_proveedor`
--

LOCK TABLES `aplicacion_credito_proveedor` WRITE;
/*!40000 ALTER TABLE `aplicacion_credito_proveedor` DISABLE KEYS */;
/*!40000 ALTER TABLE `aplicacion_credito_proveedor` ENABLE KEYS */;
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
  `precio_venta_con_itbis` decimal(12,2) DEFAULT NULL,
  `precio_venta_anterior` decimal(12,2) DEFAULT NULL,
  `exento_itbis` bit(1) NOT NULL DEFAULT b'0',
  `inventariable` bit(1) NOT NULL,
  `permitir_venta_sin_existencia` bit(1) NOT NULL DEFAULT b'0',
  `tipo_articulo` int DEFAULT NULL,
  `margen_beneficio` decimal(12,4) DEFAULT NULL,
  `porciento_utilidad` decimal(9,4) DEFAULT NULL,
  `modelo` varchar(25) DEFAULT NULL,
  `marca` varchar(80) DEFAULT NULL,
  `linea_articulo` int DEFAULT NULL,
  `nombre_linea` varchar(80) DEFAULT NULL,
  `para_venta` bit(1) NOT NULL,
  `para_consumo` bit(1) NOT NULL,
  `ruta_img` varchar(80) DEFAULT NULL,
  `compuesto` bit(1) NOT NULL DEFAULT b'0',
  `secuencia_documento` int DEFAULT NULL,
  `itbis_gravado` decimal(5,2) DEFAULT NULL,
  `venta_agranel` bit(1) NOT NULL DEFAULT b'0',
  `unidad_de_venta` int DEFAULT NULL,
  `creado_por` varchar(50) DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `perecedero` bit(1) NOT NULL DEFAULT b'0',
  `controla_lote` bit(1) NOT NULL DEFAULT b'0',
  `fecha_vencimiento` date DEFAULT NULL,
  `habilitado` bit(1) NOT NULL,
  `unidad_base` int DEFAULT NULL,
  `nombre_unidad_base` varchar(20) DEFAULT NULL,
  `metodo_de_costeo` int DEFAULT NULL,
  `dias_vencimiento` int DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `categoria` (`categoria`),
  KEY `sub_categoria` (`sub_categoria`),
  KEY `tipo_articulo` (`tipo_articulo`),
  KEY `fk_articulo_secuecia_doc` (`secuencia_documento`),
  KEY `fk_articulo_unidad_de_venta` (`unidad_de_venta`),
  KEY `fk_articulo_unidad_idx` (`unidad_base`),
  CONSTRAINT `fk_articulo_unidad` FOREIGN KEY (`unidad_base`) REFERENCES `unidad` (`codigo`),
  CONSTRAINT `fk_articulo_unidad_de_venta` FOREIGN KEY (`unidad_de_venta`) REFERENCES `unidad_de_venta` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articulo`
--

LOCK TABLES `articulo` WRITE;
/*!40000 ALTER TABLE `articulo` DISABLE KEYS */;
INSERT INTO `articulo` VALUES (1,0,'SALSA LINDA DE  1 KG','',NULL,NULL,0.00,NULL,NULL,50.00,60.00,NULL,NULL,_binary '\0',_binary '\0',_binary '',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '\0',_binary '\0',NULL,_binary '',1,'UNIDAD',NULL,NULL),(2,0,'LECHE RICA LISTA MIL ','',NULL,NULL,0.00,NULL,NULL,75.95,90.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '',_binary '',NULL,_binary '',1,'UNIDAD',NULL,15),(3,0,'SOPITA DOÃ‘A GALLINA',NULL,NULL,NULL,0.00,NULL,NULL,10.00,15.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '\0',_binary '\0',NULL,_binary '',6,'SOBRE',NULL,NULL),(4,0,'SAZON RANCHERO','',NULL,NULL,0.00,NULL,NULL,10.00,15.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '\0',_binary '',NULL,_binary '',1,'UNIDAD',NULL,NULL),(5,0,'LECHE NIDO','',NULL,NULL,0.00,NULL,NULL,35.00,45.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '',_binary '',NULL,_binary '',1,'UNIDAD',NULL,15),(6,0,'ARROZ PINCO DE 1LB',NULL,NULL,NULL,0.00,NULL,NULL,25.00,30.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '\0',_binary '\0',NULL,_binary '',1,'UNIDAD',NULL,NULL),(7,0,'PLATANO VERDE','',NULL,NULL,0.00,NULL,NULL,12.00,20.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '\0',_binary '\0',NULL,_binary '',1,'UNIDAD',NULL,NULL),(8,0,'QUESO CHEDA  LB',NULL,NULL,NULL,0.00,NULL,NULL,150.00,180.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',2,NULL,'2026-10-08 16:18:17',_binary '\0',_binary '\0',NULL,_binary '',NULL,NULL,NULL,NULL),(9,0,'CERVESA PRESIDENTE PQ',NULL,NULL,NULL,0.00,NULL,NULL,55.00,75.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',_binary '\0',NULL,_binary '',NULL,NULL,NULL,NULL),(12,0,'BERENJENA MORADA','',NULL,NULL,0.00,NULL,NULL,12.00,15000.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '\0',_binary '\0',NULL,_binary '\0',1,'UNIDAD',NULL,NULL),(27,0,'FOSFORO',NULL,NULL,NULL,0.00,NULL,NULL,10.00,15.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',NULL,NULL,'2026-10-08 16:18:17',_binary '\0',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,NULL),(28,0,'AVNA FRESCA',NULL,NULL,NULL,0.00,NULL,NULL,40.00,50.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',1,NULL,'2026-10-08 16:18:17',_binary '\0',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,NULL),(31,0,'QUESO GEO',NULL,NULL,NULL,0.00,NULL,NULL,10.00,15.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',NULL,NULL,'2026-10-08 16:18:17',_binary '\0',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,NULL),(32,0,'SALAMI ESTELAR',NULL,NULL,NULL,0.00,NULL,NULL,40.00,50.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',1,NULL,'2026-11-06 20:32:41',_binary '\0',_binary '\0',NULL,_binary '\0',1,'UNIDAD',NULL,NULL),(33,0,'ACEITE CRISOL ','',NULL,NULL,0.00,NULL,NULL,60.00,80.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,18.00,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '\0',_binary '\0',NULL,_binary '\0',1,'UNIDAD',NULL,NULL),(34,0,'ARROZ  LA GARZA DE  1LBS',NULL,NULL,NULL,0.00,NULL,NULL,25.00,30.00,NULL,NULL,_binary '\0',_binary '\0',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,NULL,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '\0',_binary '\0',NULL,_binary '\0',1,'UNIDAD',NULL,NULL),(35,0,'malagueta de la india','',NULL,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,NULL,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '\0',_binary '\0',NULL,_binary '\0',1,'UNIDAD',NULL,NULL),(36,0,'GUINEO VERDE','',NULL,NULL,0.00,NULL,NULL,5.00,8.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,NULL,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '\0',_binary '\0',NULL,_binary '\0',1,'UNIDAD',NULL,NULL),(37,0,'CAFE SANTO DOMINGO DE 1/2 LB','',NULL,NULL,29.00,NULL,NULL,200.00,250.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,NULL,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '\0',_binary '\0',NULL,_binary '\0',1,'UNIDAD',NULL,NULL),(38,0,'MANZANA ROJA','',NULL,NULL,11.00,NULL,NULL,50.00,70.00,NULL,NULL,_binary '\0',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,NULL,_binary '\0',1,NULL,'2026-11-17 14:04:12',_binary '',_binary '','2026-11-30',_binary '\0',1,'UNIDAD',2,14),(39,0,'tomate bacelo','',NULL,NULL,13.00,NULL,NULL,25.00,35.00,NULL,NULL,_binary '',_binary '',_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,_binary '\0',NULL,0.00,_binary '\0',2,NULL,'2026-11-16 20:12:00',_binary '',_binary '','2026-11-23',_binary '',4,'LIBRA',2,6);
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
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articulo_almacen`
--

LOCK TABLES `articulo_almacen` WRITE;
/*!40000 ALTER TABLE `articulo_almacen` DISABLE KEYS */;
INSERT INTO `articulo_almacen` VALUES (1,3,'SOPITA DOÃ‘A GALLINA',1,'ALMCEN  PRINCIPAL',0.00,5.00,999999.00,6,'SOBRE','2026-11-17 14:04:12','2026-10-15 18:57:53','admin',NULL,'A012'),(2,3,'SOPITA DOÃ‘A GALLINA',2,'DISPONIBLE PARA LA VENTA',0.00,5.00,5.00,6,'SOBRE','2026-10-15 18:57:53','2026-10-15 18:57:53','admin',NULL,'A012'),(11,27,'FOSFORO',1,'ALMCEN  PRINCIPAL',0.00,10.00,999999.00,1,'UNIDAD','2026-10-06 17:21:24','2026-10-06 17:21:24','admin',NULL,'A012'),(12,28,'AVNA FRESCA',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,1,'UNIDAD','2026-10-06 17:31:10','2026-10-06 17:31:10','admin',NULL,'A013'),(13,31,'QUESO GEO',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,4,'LIBRA','2026-10-06 17:56:50','2026-10-06 17:56:50','admin',NULL,'A012'),(14,31,'QUESO GEO',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,4,'LIBRA','2026-10-06 17:56:50','2026-10-06 17:56:50','admin',NULL,'A012'),(15,32,'SALAMI ESTELAR',1,'ALMCEN  PRINCIPAL',0.00,5.00,10.00,1,'UNIDAD','2026-10-09 15:06:21','2026-10-09 15:06:21','admin',NULL,'A012'),(16,32,'SALAMI ESTELAR',2,'DISPONIBLE PARA LA VENTA',0.00,1.00,1.00,1,'UNIDAD','2026-10-09 15:06:21','2026-10-09 15:06:21','admin',NULL,'A012'),(17,33,'ACEITE CRISOL ',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-10-29 21:33:27','admin',NULL,'A012'),(18,33,'ACEITE CRISOL ',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,1,'UNIDAD','2026-10-29 21:33:27','2026-10-29 21:33:27','admin',NULL,'A012'),(19,1,'SALSA LINDA DE  1 KG',1,'ALMCEN  PRINCIPAL',0.00,10.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-11-03 21:43:02','admin',NULL,'A012'),(20,1,'SALSA LINDA DE  1 KG',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,1,'UNIDAD','2026-11-12 17:53:54','2026-11-03 21:43:02','admin',NULL,'A012'),(21,2,'LECHE RICA LISTA MIL ',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-11-12 18:41:48','admin',NULL,'A012'),(22,2,'LECHE RICA LISTA MIL ',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-11-12 18:41:48','admin',NULL,'A012'),(23,34,'ARROZ  LA GARZA DE  1LBS',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-10-14 15:40:26','admin',NULL,'A012'),(24,34,'ARROZ  LA GARZA DE  1LBS',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-10-14 15:40:26','admin',NULL,'A012'),(25,4,'SAZON RANCHERO',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,1,'UNIDAD','2026-11-12 18:52:39','2026-11-12 18:52:39','admin',NULL,'A012'),(26,4,'SAZON RANCHERO',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-11-12 18:52:39','admin',NULL,'A012'),(27,6,'ARROZ PINCO DE 1LB',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-10-16 20:45:29','admin',NULL,'A012'),(28,6,'ARROZ PINCO DE 1LB',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,1,'UNIDAD','2026-10-16 20:45:29','2026-10-16 20:45:29','admin',NULL,'A012'),(29,5,'LECHE NIDO',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-11-12 20:34:16','admin',NULL,'A012'),(30,5,'LECHE NIDO',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-11-12 20:34:16','admin',NULL,'A012'),(33,7,'PLATANO VERDE',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-11-06 17:40:30','admin',NULL,'A012'),(34,7,'PLATANO VERDE',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-11-06 17:40:30','admin',NULL,'A012'),(35,12,'BERENJENA MORADA',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-10-29 20:21:58','admin',NULL,'A012'),(36,12,'BERENJENA MORADA',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-10-29 20:21:58','admin',NULL,'A012'),(37,35,'malagueta de la india',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-10-29 14:10:55','admin',NULL,'A012'),(38,35,'malagueta de la india',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-10-29 14:10:55','admin',NULL,'A012'),(39,36,'GUINEO VERDE',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-10-29 14:31:39','admin',NULL,'A012'),(40,36,'GUINEO VERDE',2,'DISPONIBLE PARA LA VENTA',0.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-10-29 14:31:39','admin',NULL,'A012'),(41,37,'CAFE SANTO DOMINGO DE 1/2 LB',1,'ALMCEN  PRINCIPAL',20.00,10.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-11-16 15:48:11','admin',NULL,'A012'),(42,37,'CAFE SANTO DOMINGO DE 1/2 LB',2,'DISPONIBLE PARA LA VENTA',9.00,10.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-11-16 15:48:11','admin',NULL,'A012'),(43,38,'MANZANA ROJA',1,'ALMCEN  PRINCIPAL',3.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-11-16 20:08:01','admin',NULL,'A012'),(44,38,'MANZANA ROJA',2,'DISPONIBLE PARA LA VENTA',8.00,0.00,999999.00,1,'UNIDAD','2026-11-17 14:04:12','2026-11-16 20:08:01','admin',NULL,'A012'),(45,39,'tomate bacelo',1,'ALMCEN  PRINCIPAL',0.00,0.00,999999.00,4,'LIBRA','2026-11-17 18:31:02','2026-11-17 18:31:02','admin',NULL,'A012'),(46,39,'tomate bacelo',2,'DISPONIBLE PARA LA VENTA',13.00,0.00,999999.00,4,'LIBRA','2026-11-17 18:31:02','2026-11-17 18:31:02','admin',NULL,'A012');
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
) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articulo_empaque`
--

LOCK TABLES `articulo_empaque` WRITE;
/*!40000 ALTER TABLE `articulo_empaque` DISABLE KEYS */;
INSERT INTO `articulo_empaque` VALUES (51,33,'ACEITE CRISOL ',1,'UNIDAD','TERMO',1.00,'7100',60.00,80.00,0,1,1,1,NULL,'admin'),(52,33,'ACEITE CRISOL',3,'CAJA','CAJA',10.00,'7001',600.00,800.00,1,1,0,1,NULL,'admin'),(53,1,'SALSA LINDA DE  1 KG',1,'UNIDAD','LATA',1.00,'2102',50.00,60.00,1,1,1,1,NULL,'admin'),(54,32,'SALAMI ESTELAR',1,'UNIDAD','UNIDAD',1.00,'2001',250.00,300.00,1,1,1,1,NULL,'admin'),(55,32,'SALAMI ESTELAR',2,'PAQUETE','PAQUETE',5.00,'20002',1250.00,1500.00,1,0,0,1,NULL,'admin'),(62,2,'LECHE RICA LISTA MIL ',1,'UNIDAD','CAJITA DE 1L',1.00,'20009',75.95,70.00,0,1,1,1,NULL,'admin'),(63,2,'LECHE RICA LISTA MIL',3,'CAJA','CAJA DE 20',10.00,'200010',759.50,700.00,1,1,0,1,NULL,'admin'),(64,2,'LECHE RICA LISTA MIL ',2,'PAQUETE','PAQUETE DE  5',5.00,'200011',379.75,300.00,1,1,0,1,NULL,'admin'),(65,32,'SALAMI ESTELAR',7,'FALDO','FALDO',4.00,'200012',1600.00,1800.00,1,1,0,1,NULL,'admin'),(66,34,'ARROZ  LA GARZA DE  1LBS',1,'UNIDAD','UNIDAD',1.00,'1001',25.00,30.00,1,1,1,1,NULL,'admin'),(67,34,'ARROZ  LA GARZA DE  1LBS',7,'FALDO','FALDO',10.00,'1002',250.00,300.00,1,0,0,1,NULL,'admin'),(68,4,'SAZON RANCHERO',1,'UNIDAD','UNIDAD',1.00,NULL,10.00,15.00,1,1,1,1,NULL,'admin'),(69,6,'ARROZ PINCO DE 1LB',1,'UNIDAD','UNIDAD',1.00,'',25.00,30.00,0,1,1,1,NULL,'admin'),(70,6,'ARROZ PINCO DE 1LB',5,'SACO','SACO',300.00,'30012',7500.00,9000.00,1,0,0,1,NULL,'admin'),(71,5,'LECHE NIDO',1,'UNIDAD','Lata de  100 gramo',1.00,'1321',35.00,45.00,0,1,1,1,NULL,'admin'),(72,5,'LECHE NIDO DE UN LT',3,'CAJA','CAJA',15.00,'1231',525.00,675.00,1,0,0,1,NULL,'admin'),(73,3,'SOPITA DOÃ‘A GALLINA',6,'SOBRE','SOBRE',1.00,'3331',10.00,15.00,0,1,1,1,NULL,'admin'),(74,3,'SOPITA DOÃ‘A GALLINA',3,'CAJA','CAJA',48.00,'33321',480.00,720.00,1,1,0,1,NULL,'admin'),(75,7,'PLATANO VERDE',1,'UNIDAD','UNIDAD',1.00,'1',12.00,20.00,1,1,1,NULL,NULL,'admin'),(76,12,'BERENJENA MORADA',1,'UNIDAD','UNIDAD',1.00,'3212',15.00,20.00,1,1,1,NULL,NULL,'admin'),(77,35,'malagueta de la india',1,'UNIDAD','FUNDITA DE 1GRM',1.00,'11000',10.00,15.00,1,1,1,NULL,NULL,'admin'),(78,36,'GUINEO VERDE',1,'UNIDAD','UNIDAD',1.00,'2132',5.00,8.00,1,1,1,NULL,NULL,'admin'),(79,36,'GUINEO VERDE',3,'CAJA','CAJA DE X20',20.00,'36214',100.00,160.00,1,1,0,NULL,NULL,'admin'),(80,37,'CAFE SANTO DOMINGO DE 1/2 LB',1,'UNIDAD','PAQUETE',1.00,'13214',200.00,250.00,1,1,1,NULL,NULL,'admin'),(81,37,'CAFE SANTO DOMINGO DE 1/2 LB',3,'CAJA','CAJA',10.00,'1321463',2000.00,2500.00,1,1,0,NULL,NULL,'admin'),(82,38,'MANZANA ROJA',1,'UNIDAD',NULL,1.00,'',50.00,70.00,1,1,1,NULL,NULL,'admin'),(83,39,'tomate bacelo',4,'LIBRA',NULL,1.00,'',25.00,35.00,1,1,1,NULL,NULL,'admin');
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
  `monto_apertura` decimal(12,2) NOT NULL,
  `fecha_cierre` datetime DEFAULT NULL,
  `monto_cierre` decimal(12,2) DEFAULT NULL,
  `monto_cierre_esperado` decimal(12,2) DEFAULT NULL,
  `monto_cierre_real` decimal(12,2) DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `caja_turno`
--

LOCK TABLES `caja_turno` WRITE;
/*!40000 ALTER TABLE `caja_turno` DISABLE KEYS */;
INSERT INTO `caja_turno` VALUES (1,'2026-11-17 10:09:41',NULL,10000.00,NULL,NULL,NULL,NULL,NULL,'Admin',NULL,'Abierta','Apertura con monto mayor');
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
  `limite_credito` decimal(12,2) DEFAULT NULL,
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00',
  `apodo` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cliente`
--

LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` VALUES (13,'PUBLICO GENERAL','00','00','EMPRESA','EMPRESA',NULL,'EMPRESA','2025-09-07 10:35:40',NULL,'2025-09-07 10:35:40',_binary '',NULL,0.00,NULL),(14,'MAXIMILIANO ALMONTE RODRIGUEZ','034-0044420-8','809-280-7944','Las colinas santiago  c/29 #5','cementosc ibao','Admin','nino','2026-08-25 10:02:58',NULL,'2026-11-16 14:38:01',_binary '',5000.00,210.00,NULL),(15,'JOSE ALMANZAR','0000','809-280-4521','25-10 LAS COLINAS SANTIAGO','','Admin','','2026-09-11 11:03:35',NULL,'2026-11-12 09:42:05',_binary '',NULL,0.00,NULL),(16,'RAMON PERZ','','','CLLE 29 CASA 5 LAS COLINAS','',NULL,'','2026-10-28 17:54:01',NULL,'2026-10-28 17:54:01',_binary '',NULL,0.00,NULL);
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `condicion_pago`
--

DROP TABLE IF EXISTS `condicion_pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `condicion_pago` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) NOT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `condicion_pago`
--

LOCK TABLES `condicion_pago` WRITE;
/*!40000 ALTER TABLE `condicion_pago` DISABLE KEYS */;
INSERT INTO `condicion_pago` VALUES (1,'DE CONTADO'),(2,'CREDITO');
/*!40000 ALTER TABLE `condicion_pago` ENABLE KEYS */;
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
-- Table structure for table `cuenta_motorista`
--

DROP TABLE IF EXISTS `cuenta_motorista`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cuenta_motorista` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `delivery` int NOT NULL,
  `fecha` datetime NOT NULL,
  `tipo` varchar(30) NOT NULL,
  `monto` decimal(18,2) NOT NULL,
  `liquidacion_delivery` int DEFAULT NULL,
  `descripcion` varchar(300) DEFAULT NULL,
  `nombre_usuario` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `idx_cuenta_motorista_delivery` (`delivery`,`fecha`),
  KEY `idx_cuenta_motorista_liq` (`liquidacion_delivery`),
  CONSTRAINT `fk_cuenta_motorista_delivery` FOREIGN KEY (`delivery`) REFERENCES `delivery` (`codigo`),
  CONSTRAINT `fk_cuenta_motorista_liq` FOREIGN KEY (`liquidacion_delivery`) REFERENCES `liquidacion_delivery` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cuenta_motorista`
--

LOCK TABLES `cuenta_motorista` WRITE;
/*!40000 ALTER TABLE `cuenta_motorista` DISABLE KEYS */;
/*!40000 ALTER TABLE `cuenta_motorista` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cuenta_por_cobrar`
--

DROP TABLE IF EXISTS `cuenta_por_cobrar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cuenta_por_cobrar` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `cliente` int NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `tipo_movimiento` varchar(30) NOT NULL COMMENT 'FACTURA, RECIBO, ANULACION_FACTURA, DEVOLUCION, AJUSTE_DEBITO, AJUSTE_CREDITO',
  `factura` int DEFAULT NULL,
  `recibo_ingreso` int DEFAULT NULL,
  `numero_documento` varchar(50) DEFAULT NULL,
  `concepto` varchar(255) DEFAULT NULL,
  `debito` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT 'Aumenta lo que debe el cliente',
  `credito` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT 'Disminuye lo que debe el cliente',
  `balance_anterior` decimal(12,2) NOT NULL DEFAULT '0.00',
  `balance_nuevo` decimal(12,2) NOT NULL DEFAULT '0.00',
  `usuario` varchar(100) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`codigo`),
  KEY `idx_cxc_cliente_fecha` (`cliente`,`fecha`),
  KEY `idx_cxc_factura` (`factura`),
  KEY `idx_cxc_recibo` (`recibo_ingreso`),
  CONSTRAINT `fk_cxc_cliente` FOREIGN KEY (`cliente`) REFERENCES `cliente` (`codigo`),
  CONSTRAINT `fk_cxc_factura` FOREIGN KEY (`factura`) REFERENCES `factura_de_venta` (`codigo`),
  CONSTRAINT `fk_cxc_recibo` FOREIGN KEY (`recibo_ingreso`) REFERENCES `recibo_de_ingreso` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cuenta_por_cobrar`
--

LOCK TABLES `cuenta_por_cobrar` WRITE;
/*!40000 ALTER TABLE `cuenta_por_cobrar` DISABLE KEYS */;
INSERT INTO `cuenta_por_cobrar` VALUES (1,14,'2026-11-17 10:19:44','FACTURA',3,NULL,'FAC-3','Factura a crÃ©dito #3',250.00,0.00,0.00,250.00,'Administrador','2026-11-17 10:19:44'),(2,14,'2026-11-17 10:25:13','RECIBO',3,3,'RI-3','Pago recibo #3 a factura #3',0.00,250.00,250.00,0.00,'Admin','2026-11-17 10:25:13'),(3,14,'2026-11-17 11:43:07','FACTURA',4,NULL,'FAC-4','Factura a crÃ©dito #4',105.00,0.00,0.00,105.00,'Administrador','2026-11-17 11:43:07'),(4,14,'2026-11-17 14:32:11','FACTURA',5,NULL,'FAC-5','Factura a crÃ©dito #5',105.00,0.00,105.00,210.00,'Administrador','2026-11-17 14:32:12');
/*!40000 ALTER TABLE `cuenta_por_cobrar` ENABLE KEYS */;
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
  `celular` varchar(15) NOT NULL,
  `direccion` varchar(80) NOT NULL,
  `fecha` date NOT NULL,
  `habilitado` bit(1) DEFAULT b'1',
  `cedula` varchar(15) DEFAULT NULL,
  `vehiculo` int DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_delivery_vehiculo_idx` (`vehiculo`),
  CONSTRAINT `fk_delivery_vehiculo` FOREIGN KEY (`vehiculo`) REFERENCES `vehiculo` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery`
--

LOCK TABLES `delivery` WRITE;
/*!40000 ALTER TABLE `delivery` DISABLE KEYS */;
INSERT INTO `delivery` VALUES (1,'RAMON ALMONTE','809-251-4521','LAS COLINAS','2026-10-30',_binary '','12',NULL),(2,'JOSE CARRAZCO','000','GURAVITO','2026-08-26',_binary '','1',NULL),(3,'carlos rodriguez','809-542-8754','los salado','2026-10-30',_binary '','1',2),(4,'YONDY ESTRELLA','809-214-2010','LUGUNA PREITA','2026-10-30',_binary '','034-12132514',1);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `lote` int DEFAULT NULL,
  `numero_lote` varchar(50) DEFAULT NULL,
  `fecha_vencimiento` date DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_det_ajusute_articulo` (`articulo`),
  KEY `fk_det_ajuste_inventario` (`ajuste_inventario`),
  KEY `fk_det_ajuste_unidad` (`unidad`),
  KEY `fk_det_ajusute_almacen` (`almacen`),
  KEY `fk_det_ajusute_art_empq_idx` (`articulo_empaque`),
  KEY `idx_det_aju_lote` (`lote`),
  CONSTRAINT `fk_det_aju_lote` FOREIGN KEY (`lote`) REFERENCES `lote` (`codigo`),
  CONSTRAINT `fk_det_ajuste_inventario` FOREIGN KEY (`ajuste_inventario`) REFERENCES `ajuste_inventario` (`codigo`),
  CONSTRAINT `fk_det_ajuste_unidad` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`),
  CONSTRAINT `fk_det_ajusute_almacen` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_det_ajusute_art_empq` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_det_ajusute_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_ajuste_inventario`
--

LOCK TABLES `detalle_ajuste_inventario` WRITE;
/*!40000 ALTER TABLE `detalle_ajuste_inventario` DISABLE KEYS */;
INSERT INTO `detalle_ajuste_inventario` VALUES (1,1,38,'MANZANA ROJA',0.00,10.00,10.00,1,'UNIDAD',1,'ALMCEN  PRINCIPAL',82,1.00,10.00,50.00,50.00,50.00,0.00,50.00,NULL,NULL,NULL);
/*!40000 ALTER TABLE `detalle_ajuste_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_devolucion_proveedor`
--

DROP TABLE IF EXISTS `detalle_devolucion_proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_devolucion_proveedor` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `devolucion` int NOT NULL,
  `articulo` int NOT NULL,
  `articulo_empaque` int NOT NULL,
  `descripcion_articulo` varchar(250) DEFAULT NULL,
  `cantidad` decimal(12,4) NOT NULL,
  `factor_conversion` decimal(12,4) NOT NULL DEFAULT '1.0000',
  `cantidad_base` decimal(12,4) NOT NULL,
  `costo_unitario` decimal(12,4) NOT NULL DEFAULT '0.0000',
  `porcentaje_itbis` decimal(5,2) NOT NULL DEFAULT '0.00',
  `sub_total` decimal(14,2) NOT NULL DEFAULT '0.00',
  `itbis` decimal(14,2) NOT NULL DEFAULT '0.00',
  `total` decimal(14,2) NOT NULL DEFAULT '0.00',
  `lote` int DEFAULT NULL,
  `numero_lote_nuevo` varchar(50) DEFAULT NULL,
  `fecha_vencimiento_nueva` date DEFAULT NULL,
  `lote_nuevo` int DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `idx_detdevprov_devolucion` (`devolucion`),
  KEY `fk_detdevprov_articulo` (`articulo`),
  KEY `fk_detdevprov_empaque` (`articulo_empaque`),
  KEY `fk_detdevprov_lote` (`lote`),
  KEY `fk_detdevprov_lote_nuevo` (`lote_nuevo`),
  CONSTRAINT `fk_detdevprov_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `fk_detdevprov_devolucion` FOREIGN KEY (`devolucion`) REFERENCES `devolucion_proveedor` (`codigo`),
  CONSTRAINT `fk_detdevprov_empaque` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_detdevprov_lote` FOREIGN KEY (`lote`) REFERENCES `lote` (`codigo`),
  CONSTRAINT `fk_detdevprov_lote_nuevo` FOREIGN KEY (`lote_nuevo`) REFERENCES `lote` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_devolucion_proveedor`
--

LOCK TABLES `detalle_devolucion_proveedor` WRITE;
/*!40000 ALTER TABLE `detalle_devolucion_proveedor` DISABLE KEYS */;
/*!40000 ALTER TABLE `detalle_devolucion_proveedor` ENABLE KEYS */;
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
  `lote` int DEFAULT NULL,
  `numero_lote` varchar(50) DEFAULT NULL,
  `fecha_vencimiento` date DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_det_entrada_inv_articulo` (`articulo`) USING BTREE,
  KEY `fk_det_entrada_inv_entrada_inv` (`entrada_inventario`) USING BTREE,
  KEY `fk_det_entrada_inv_unidad` (`unidad`) USING BTREE,
  KEY `detalle_entrada_inventario_ibfk_5` (`almacen`),
  KEY `fk_ent_det_inv_art_empq_idx` (`articulo_empaque`),
  KEY `idx_det_ent_lote` (`lote`),
  CONSTRAINT `fk_det_ent_inv_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `fk_det_ent_inv_entrada` FOREIGN KEY (`entrada_inventario`) REFERENCES `entrada_inventario` (`codigo`),
  CONSTRAINT `fk_det_ent_inv_unidad` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`),
  CONSTRAINT `fk_det_ent_lote` FOREIGN KEY (`lote`) REFERENCES `lote` (`codigo`),
  CONSTRAINT `fk_ent_det_inv_almacen` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_ent_det_inv_art_empq` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_entrada_inventario`
--

LOCK TABLES `detalle_entrada_inventario` WRITE;
/*!40000 ALTER TABLE `detalle_entrada_inventario` DISABLE KEYS */;
INSERT INTO `detalle_entrada_inventario` VALUES (1,1,39,'tomate bacelo',20.00,1.00,20.00,4,'LIBRA',83,0.00,0.00,25.00,25.00,0.00,20.00,NULL,1,'ALMCEN  PRINCIPAL',500.00,90.00,590.00,1,'L20261117','2026-11-23'),(2,2,39,'tomate bacelo',20.00,1.00,20.00,4,'LIBRA',83,20.00,0.00,25.00,25.00,0.00,20.00,NULL,2,'DISPONIBLE PARA LA VENTA',0.00,0.00,0.00,1,'L20261117','2026-11-23'),(3,3,38,'MANZANA ROJA',10.00,1.00,10.00,1,'UNIDAD',82,0.00,0.00,50.00,50.00,0.00,10.00,NULL,1,'ALMCEN  PRINCIPAL',50.00,0.00,50.00,2,'L20261117','2026-12-01'),(4,4,38,'MANZANA ROJA',10.00,1.00,10.00,1,'UNIDAD',82,10.00,0.00,50.00,50.00,0.00,10.00,NULL,2,'DISPONIBLE PARA LA VENTA',0.00,0.00,0.00,2,'L20261117','2026-12-01'),(5,5,37,'CAFE SANTO DOMINGO DE 1/2 LB',3.00,10.00,30.00,3,'CAJA',81,0.00,0.00,2000.00,2000.00,0.00,30.00,NULL,1,'ALMCEN  PRINCIPAL',6000.00,1080.00,7080.00,NULL,NULL,NULL),(6,6,37,'CAFE SANTO DOMINGO DE 1/2 LB',10.00,1.00,20.00,1,'UNIDAD',80,10.00,0.00,200.00,200.00,0.00,20.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL,NULL,NULL,NULL,NULL,NULL),(7,7,38,'MANZANA ROJA',3.00,1.00,3.00,1,'UNIDAD',82,0.00,0.00,50.00,50.00,8.00,11.00,NULL,1,'ALMCEN  PRINCIPAL',150.00,27.00,177.00,2,'L20261117','2026-12-01');
/*!40000 ALTER TABLE `detalle_entrada_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_factura_de_compra`
--

DROP TABLE IF EXISTS `detalle_factura_de_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_factura_de_compra` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `factura_de_compra` int NOT NULL,
  `articulo` int DEFAULT NULL,
  `descripcion_articulo` varchar(80) DEFAULT NULL,
  `articulo_empaque` int DEFAULT NULL,
  `factor_conversion` decimal(12,2) DEFAULT NULL,
  `cantidad_fisica_base` decimal(12,2) DEFAULT NULL,
  `unidad` int DEFAULT NULL,
  `cantidad` decimal(12,2) DEFAULT NULL,
  `precio_unitario` decimal(12,2) DEFAULT NULL,
  `sub_total` decimal(12,2) DEFAULT NULL,
  `porciento_itbis` decimal(12,2) DEFAULT NULL,
  `porciento_isr` decimal(12,2) DEFAULT NULL,
  `porciento_descuento` decimal(12,2) DEFAULT NULL,
  `total_descuento` decimal(12,2) DEFAULT NULL,
  `total_itbis` decimal(12,2) DEFAULT NULL,
  `total_isr` decimal(12,2) DEFAULT NULL,
  `otros_impuesto` decimal(12,2) DEFAULT NULL,
  `total_impuesto` decimal(12,2) DEFAULT NULL,
  `total` decimal(12,2) DEFAULT NULL,
  `total_a_pagar` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_detalle_orden_compra_orden_compra_idx` (`factura_de_compra`) USING BTREE,
  KEY `fk_detalle_orden__articulo_idx` (`articulo`) USING BTREE,
  KEY `fk_detalle_facturcom_arti_empq_idx` (`articulo_empaque`),
  CONSTRAINT `fk_detalle_factura_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `fk_detalle_facturcom_arti_empq` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_detalle_orden_compra_factura_compra` FOREIGN KEY (`factura_de_compra`) REFERENCES `factura_de_compra` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_factura_de_compra`
--

LOCK TABLES `detalle_factura_de_compra` WRITE;
/*!40000 ALTER TABLE `detalle_factura_de_compra` DISABLE KEYS */;
INSERT INTO `detalle_factura_de_compra` VALUES (2,2,39,'tomate bacelo',83,1.00,20.00,4,20.00,25.00,500.00,18.00,0.00,0.00,10.00,90.00,90.00,0.00,NULL,590.00,590.00),(3,3,37,'CAFE SANTO DOMINGO DE 1/2 LB',81,10.00,30.00,3,3.00,2000.00,6000.00,18.00,0.00,0.00,10.00,1080.00,1080.00,0.00,NULL,7080.00,7080.00),(4,4,38,'MANZANA ROJA',82,1.00,3.00,1,3.00,50.00,150.00,18.00,0.00,0.00,10.00,27.00,27.00,0.00,NULL,177.00,177.00),(5,5,38,'MANZANA ROJA',82,1.00,NULL,1,3.00,50.00,150.00,0.00,NULL,0.00,0.00,0.00,NULL,NULL,NULL,150.00,NULL);
/*!40000 ALTER TABLE `detalle_factura_de_compra` ENABLE KEYS */;
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
  `precio_compra` decimal(12,2) DEFAULT NULL,
  `existencia_actual` decimal(12,2) DEFAULT NULL,
  `nueva_existencia` decimal(12,2) DEFAULT NULL,
  `almacen` int DEFAULT NULL,
  `nombre_almacen` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `precio_venta` decimal(12,2) DEFAULT NULL,
  `cantidad` decimal(12,2) NOT NULL,
  `sub_total` decimal(12,2) NOT NULL,
  `total_descuento` decimal(12,2) NOT NULL,
  `total_itbis` decimal(12,2) NOT NULL,
  `total` decimal(12,2) NOT NULL,
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
  CONSTRAINT `fk_det_fact_factura` FOREIGN KEY (`factura`) REFERENCES `factura_de_venta` (`codigo`),
  CONSTRAINT `fk_ent_det_inv_almacen_copy` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_factura_de_venta`
--

LOCK TABLES `detalle_factura_de_venta` WRITE;
/*!40000 ALTER TABLE `detalle_factura_de_venta` DISABLE KEYS */;
INSERT INTO `detalle_factura_de_venta` VALUES (1,1,39,'tomate bacelo',4,'LIBRA',83,1.00,2.00,25.00,0.00,NULL,2,'DISPONIBLE PARA LA VENTA',35.00,2.00,70.00,0.00,12.60,82.60,0.00,18.00,1,0.00),(2,2,38,'MANZANA ROJA',1,'UNIDAD',82,1.00,1.00,50.00,0.00,NULL,2,'DISPONIBLE PARA LA VENTA',70.00,1.00,70.00,0.00,12.60,82.60,0.00,18.00,1,0.00),(3,3,37,'CAFE SANTO DOMINGO DE 1/2 LB PAQUETE',1,'UNIDAD',80,1.00,1.00,200.00,0.00,NULL,2,'DISPONIBLE PARA LA VENTA',250.00,1.00,250.00,0.00,45.00,295.00,0.00,18.00,1,0.00),(4,4,39,'tomate bacelo',4,'LIBRA',83,1.00,1.00,25.00,18.00,NULL,2,'DISPONIBLE PARA LA VENTA',35.00,1.00,35.00,0.00,5.34,35.00,0.00,18.00,1,0.00),(5,4,38,'MANZANA ROJA',1,'UNIDAD',82,1.00,1.00,50.00,9.00,NULL,2,'DISPONIBLE PARA LA VENTA',70.00,1.00,70.00,0.00,10.68,70.00,0.00,18.00,2,0.00),(6,5,39,'tomate bacelo',4,'LIBRA',83,1.00,3.00,25.00,17.00,NULL,2,'DISPONIBLE PARA LA VENTA',35.00,3.00,105.00,0.00,0.00,105.00,0.00,0.00,1,0.00),(7,6,39,'tomate bacelo',4,'LIBRA',83,1.00,1.00,25.00,14.00,NULL,2,'DISPONIBLE PARA LA VENTA',35.00,1.00,35.00,0.00,0.00,35.00,0.00,0.00,1,0.00);
/*!40000 ALTER TABLE `detalle_factura_de_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_factura_venta_lote`
--

DROP TABLE IF EXISTS `detalle_factura_venta_lote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_factura_venta_lote` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `detalle_factura_de_venta` int NOT NULL,
  `lote` int NOT NULL,
  `almacen` int NOT NULL,
  `cantidad` decimal(12,2) NOT NULL COMMENT 'unidad base',
  `cantidad_devuelta` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT 'unidad base ya repuesta por devoluciÃ³n/anulaciÃ³n',
  `lote_vencido` bit(1) NOT NULL DEFAULT b'0' COMMENT 'se vendiÃ³ con el lote vencido',
  `autorizado_por` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'supervisor que autorizÃ³ vender el lote vencido',
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`codigo`),
  KEY `idx_dfvl_detalle` (`detalle_factura_de_venta`),
  KEY `idx_dfvl_lote` (`lote`),
  KEY `fk_dfvl_almacen` (`almacen`),
  CONSTRAINT `fk_dfvl_almacen` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_dfvl_detalle` FOREIGN KEY (`detalle_factura_de_venta`) REFERENCES `detalle_factura_de_venta` (`codigo`),
  CONSTRAINT `fk_dfvl_lote` FOREIGN KEY (`lote`) REFERENCES `lote` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_factura_venta_lote`
--

LOCK TABLES `detalle_factura_venta_lote` WRITE;
/*!40000 ALTER TABLE `detalle_factura_venta_lote` DISABLE KEYS */;
INSERT INTO `detalle_factura_venta_lote` VALUES (1,1,1,2,2.00,0.00,_binary '\0',NULL,'2026-11-17 10:11:16'),(2,2,2,2,1.00,0.00,_binary '\0',NULL,'2026-11-17 10:16:22'),(3,4,1,2,1.00,0.00,_binary '\0',NULL,'2026-11-17 11:43:07'),(4,5,2,2,1.00,0.00,_binary '\0',NULL,'2026-11-17 11:43:07'),(5,6,1,2,3.00,0.00,_binary '\0',NULL,'2026-11-17 14:32:12'),(6,7,1,2,1.00,0.00,_binary '\0',NULL,'2026-11-17 17:53:19');
/*!40000 ALTER TABLE `detalle_factura_venta_lote` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_liquidacion_delivery`
--

DROP TABLE IF EXISTS `detalle_liquidacion_delivery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_liquidacion_delivery` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `liquidacion_delivery` int NOT NULL,
  `factura` int NOT NULL,
  `tipo_pago` varchar(10) NOT NULL,
  `monto` decimal(18,2) NOT NULL,
  `cliente` int DEFAULT NULL,
  `anulada` tinyint(1) NOT NULL DEFAULT '0',
  `factura_activa` int GENERATED ALWAYS AS (if((`anulada` = 0),`factura`,NULL)) STORED,
  PRIMARY KEY (`codigo`),
  UNIQUE KEY `uk_detalle_liq_factura_activa` (`factura_activa`),
  KEY `idx_detalle_liq_liquidacion` (`liquidacion_delivery`),
  KEY `fk_detalle_liq_cliente_liq_idx` (`codigo`,`cliente`),
  KEY `fk_detalle_liq_liquidacion_cliente_idx` (`cliente`),
  KEY `idx_detalle_liq_factura` (`factura`),
  CONSTRAINT `fk_detalle_liq_factura` FOREIGN KEY (`factura`) REFERENCES `factura_de_venta` (`codigo`),
  CONSTRAINT `fk_detalle_liq_liquidacion` FOREIGN KEY (`liquidacion_delivery`) REFERENCES `liquidacion_delivery` (`codigo`),
  CONSTRAINT `fk_detalle_liq_liquidacion_cliente` FOREIGN KEY (`cliente`) REFERENCES `cliente` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_liquidacion_delivery`
--

LOCK TABLES `detalle_liquidacion_delivery` WRITE;
/*!40000 ALTER TABLE `detalle_liquidacion_delivery` DISABLE KEYS */;
INSERT INTO `detalle_liquidacion_delivery` (`codigo`, `liquidacion_delivery`, `factura`, `tipo_pago`, `monto`, `cliente`, `anulada`) VALUES (1,1,1,'CONTADO',70.00,NULL,0);
/*!40000 ALTER TABLE `detalle_liquidacion_delivery` ENABLE KEYS */;
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
  `lote` int DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_detalle_merma_merma` (`merma`),
  KEY `fk_detalle_merma_articulo` (`articulo`),
  KEY `fk_detalle_merma_articulo_empaque` (`articulo_empaque`),
  KEY `idx_det_mer_lote` (`lote`),
  CONSTRAINT `fk_det_mer_lote` FOREIGN KEY (`lote`) REFERENCES `lote` (`codigo`),
  CONSTRAINT `fk_detalle_merma_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `fk_detalle_merma_articulo_empaque` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_detalle_merma_merma` FOREIGN KEY (`merma`) REFERENCES `merma` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_merma`
--

LOCK TABLES `detalle_merma` WRITE;
/*!40000 ALTER TABLE `detalle_merma` DISABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_ordende_de_compra`
--

LOCK TABLES `detalle_ordende_de_compra` WRITE;
/*!40000 ALTER TABLE `detalle_ordende_de_compra` DISABLE KEYS */;
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
  `lote` int DEFAULT NULL,
  `numero_lote` varchar(50) DEFAULT NULL,
  `fecha_vencimiento` date DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_det_rec_articulo` (`articulo`),
  KEY `fk_det_recepcion_mercancia_idx` (`codigo`,`recepcion_mercancia`),
  KEY `fk_det_recepcion_unidad_idx` (`unidad`),
  KEY `fk_det_recepcion_recepcion_idx` (`recepcion_mercancia`),
  KEY `fk_det_recepcion_art_empaque_idx` (`articulo_empaque`),
  KEY `fk_det_recepcion_alm_idx` (`almacen`),
  KEY `idx_det_rec_lote` (`lote`),
  CONSTRAINT `fk_det_rec_lote` FOREIGN KEY (`lote`) REFERENCES `lote` (`codigo`),
  CONSTRAINT `fk_det_recepcion_alm` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_det_recepcion_art_empaque` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_det_recepcion_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `fk_det_recepcion_recepcion` FOREIGN KEY (`recepcion_mercancia`) REFERENCES `recepcion_mercancia` (`codigo`),
  CONSTRAINT `fk_det_recepcion_unidad` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_recepcion_mercancia`
--

LOCK TABLES `detalle_recepcion_mercancia` WRITE;
/*!40000 ALTER TABLE `detalle_recepcion_mercancia` DISABLE KEYS */;
INSERT INTO `detalle_recepcion_mercancia` VALUES (2,2,39,'tomate bacelo','LIBRA',4,20.00,25.00,500.00,90.00,590.00,83,1.00,20.00,1,1,'L20261117','2026-11-23'),(3,3,37,'CAFE SANTO DOMINGO DE 1/2 LB','CAJA',3,3.00,2000.00,6000.00,1080.00,7080.00,81,10.00,30.00,1,NULL,NULL,NULL),(4,4,38,'MANZANA ROJA','UNIDAD',1,3.00,50.00,150.00,27.00,177.00,82,1.00,3.00,1,2,'L20261117','2026-12-01');
/*!40000 ALTER TABLE `detalle_recepcion_mercancia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_recibo_de_egreso`
--

DROP TABLE IF EXISTS `detalle_recibo_de_egreso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_recibo_de_egreso` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `recibo_egreso` int DEFAULT NULL,
  `factura` int DEFAULT NULL,
  `total` decimal(12,2) DEFAULT NULL,
  `balance_pendiente_antes` decimal(12,2) DEFAULT NULL,
  `balance_pendiente_despues` decimal(12,2) DEFAULT NULL,
  `concepto` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_recibo_egreso` (`recibo_egreso`) USING BTREE,
  KEY `fk_factura5` (`factura`) USING BTREE,
  KEY `FK3BFADB205E043917` (`recibo_egreso`) USING BTREE,
  KEY `FK3BFADB2032CF67C4` (`factura`) USING BTREE,
  CONSTRAINT `detalle_recibo_egreso_ibfk_1` FOREIGN KEY (`factura`) REFERENCES `factura_de_compra` (`codigo`),
  CONSTRAINT `detalle_recibo_egreso_ibfk_2` FOREIGN KEY (`recibo_egreso`) REFERENCES `recibo_de_egreso` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_recibo_de_egreso`
--

LOCK TABLES `detalle_recibo_de_egreso` WRITE;
/*!40000 ALTER TABLE `detalle_recibo_de_egreso` DISABLE KEYS */;
INSERT INTO `detalle_recibo_de_egreso` VALUES (1,1,2,590.00,590.00,0.00,NULL),(2,2,3,7080.00,7080.00,0.00,NULL),(3,3,4,177.00,177.00,0.00,NULL);
/*!40000 ALTER TABLE `detalle_recibo_de_egreso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_recibo_de_ingreso`
--

DROP TABLE IF EXISTS `detalle_recibo_de_ingreso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_recibo_de_ingreso` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `recibo_ingreso` int DEFAULT NULL,
  `factura` int DEFAULT NULL,
  `total` decimal(12,2) DEFAULT NULL,
  `balance_pendiente_antes` decimal(12,2) DEFAULT NULL,
  `balance_pendiente_despues` decimal(12,2) DEFAULT NULL,
  `concepto` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_recibo_ingreso` (`recibo_ingreso`) USING BTREE,
  KEY `fk_factura5` (`factura`) USING BTREE,
  KEY `FK3BFADB205E043917` (`recibo_ingreso`) USING BTREE,
  KEY `FK3BFADB2032CF67C4` (`factura`) USING BTREE,
  CONSTRAINT `detalle_recibo_ingreso_ibfk_1` FOREIGN KEY (`factura`) REFERENCES `factura_de_venta` (`codigo`),
  CONSTRAINT `detalle_recibo_ingreso_ibfk_2` FOREIGN KEY (`recibo_ingreso`) REFERENCES `recibo_de_ingreso` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_recibo_de_ingreso`
--

LOCK TABLES `detalle_recibo_de_ingreso` WRITE;
/*!40000 ALTER TABLE `detalle_recibo_de_ingreso` DISABLE KEYS */;
INSERT INTO `detalle_recibo_de_ingreso` VALUES (1,1,2,70.00,70.00,0.00,'PAgo Factura de venta #2'),(2,2,1,70.00,70.00,0.00,'Pago total a factura en este recibo '),(3,3,3,250.00,250.00,0.00,'Pago total a factura en este recibo '),(4,4,6,35.00,35.00,0.00,'PAgo Factura de venta #6');
/*!40000 ALTER TABLE `detalle_recibo_de_ingreso` ENABLE KEYS */;
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
  `lote` int DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_det_salida_idx` (`salida_inventario`),
  KEY `fk_det_articulo_idx` (`articulo`),
  KEY `fk_det_unidad_idx` (`unidad`),
  KEY `fk_det_almacen_idx` (`almacen`),
  KEY `fk_det_art_empq_idx` (`articulo_empaque`),
  KEY `idx_det_sal_lote` (`lote`),
  CONSTRAINT `fk_det_almacen` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_det_art_empq` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_det_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `fk_det_sal_lote` FOREIGN KEY (`lote`) REFERENCES `lote` (`codigo`),
  CONSTRAINT `fk_det_salida` FOREIGN KEY (`salida_inventario`) REFERENCES `salida_inventario` (`codigo`),
  CONSTRAINT `fk_det_unidad` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_salida_inventario`
--

LOCK TABLES `detalle_salida_inventario` WRITE;
/*!40000 ALTER TABLE `detalle_salida_inventario` DISABLE KEYS */;
INSERT INTO `detalle_salida_inventario` VALUES (1,1,39,'tomate bacelo',4,83,1.00,20.00,20.00,NULL,NULL,NULL,0.00,25.00,35.00,25.00,20.00,NULL,1,NULL,1),(2,2,39,'tomate bacelo',4,83,1.00,2.00,2.00,70.00,12.60,82.60,18.00,25.00,35.00,25.00,20.00,NULL,2,'DISPONIBLE PARA LA VENTA',1),(3,3,38,'MANZANA ROJA',1,82,1.00,10.00,10.00,NULL,NULL,NULL,0.00,50.00,70.00,50.00,10.00,NULL,1,NULL,2),(4,4,38,'MANZANA ROJA',1,82,1.00,1.00,1.00,70.00,12.60,82.60,9.00,50.00,70.00,50.00,10.00,NULL,2,'DISPONIBLE PARA LA VENTA',2),(5,5,37,'CAFE SANTO DOMINGO DE 1/2 LB',1,80,1.00,10.00,10.00,NULL,NULL,NULL,20.00,200.00,250.00,200.00,30.00,NULL,1,NULL,NULL),(6,6,37,'CAFE SANTO DOMINGO DE 1/2 LB',1,80,1.00,1.00,1.00,250.00,45.00,295.00,9.00,200.00,250.00,200.00,10.00,NULL,2,'DISPONIBLE PARA LA VENTA',NULL),(7,7,39,'tomate bacelo',4,83,1.00,1.00,1.00,35.00,5.34,35.00,17.00,25.00,35.00,25.00,18.00,NULL,2,'DISPONIBLE PARA LA VENTA',1),(8,7,38,'MANZANA ROJA',1,82,1.00,1.00,1.00,70.00,10.68,70.00,8.00,50.00,70.00,50.00,9.00,NULL,2,'DISPONIBLE PARA LA VENTA',2),(15,14,39,'tomate bacelo',4,83,1.00,3.00,3.00,105.00,0.00,105.00,14.00,25.00,35.00,25.00,17.00,NULL,2,'DISPONIBLE PARA LA VENTA',1),(16,15,39,'tomate bacelo',4,83,1.00,1.00,1.00,35.00,0.00,35.00,13.00,25.00,35.00,25.00,14.00,NULL,2,'DISPONIBLE PARA LA VENTA',1);
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
  `lote` int DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_det_traslado_articulo` (`articulo`),
  KEY `fk_det_traslado_cabecera` (`traslado`),
  KEY `fk_det_traslado_unidad_idx` (`unidad`),
  KEY `fk_det_traslado_art_empq_idx` (`articulo_empaque`),
  KEY `idx_det_tra_lote` (`lote`),
  CONSTRAINT `fk_det_tra_lote` FOREIGN KEY (`lote`) REFERENCES `lote` (`codigo`),
  CONSTRAINT `fk_det_traslado_art_empq` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_det_traslado_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_det_traslado_cabecera` FOREIGN KEY (`traslado`) REFERENCES `traslado_inventario` (`codigo`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_det_traslado_unidad` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_traslado_inventario`
--

LOCK TABLES `detalle_traslado_inventario` WRITE;
/*!40000 ALTER TABLE `detalle_traslado_inventario` DISABLE KEYS */;
INSERT INTO `detalle_traslado_inventario` VALUES (1,1,39,'tomate bacelo',4,'LIBRA',20.00,20.00,25.00,25.00,83,1.00,0.00,NULL,NULL,NULL,20.00,1),(2,2,38,'MANZANA ROJA',1,'UNIDAD',10.00,10.00,50.00,50.00,82,1.00,0.00,NULL,NULL,NULL,10.00,2),(3,3,37,'CAFE SANTO DOMINGO DE 1/2 LB',1,'UNIDAD',10.00,10.00,200.00,200.00,80,1.00,20.00,NULL,NULL,NULL,30.00,NULL);
/*!40000 ALTER TABLE `detalle_traslado_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `devolucion_proveedor`
--

DROP TABLE IF EXISTS `devolucion_proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `devolucion_proveedor` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL,
  `proveedor` int NOT NULL,
  `almacen` int NOT NULL,
  `tipo_liquidacion` varchar(20) NOT NULL,
  `motivo` varchar(20) NOT NULL,
  `observacion` varchar(250) DEFAULT NULL,
  `factura_compra` int DEFAULT NULL,
  `sub_total` decimal(14,2) NOT NULL DEFAULT '0.00',
  `itbis` decimal(14,2) NOT NULL DEFAULT '0.00',
  `total` decimal(14,2) NOT NULL DEFAULT '0.00',
  `credito_aplicado` decimal(14,2) NOT NULL DEFAULT '0.00',
  `credito_disponible` decimal(14,2) NOT NULL DEFAULT '0.00',
  `caja_turno` int DEFAULT NULL,
  `salida_inventario` int DEFAULT NULL,
  `entrada_inventario` int DEFAULT NULL,
  `anulada` bit(1) NOT NULL DEFAULT b'0',
  `fecha_anulada` datetime DEFAULT NULL,
  `motivo_anulacion` varchar(250) DEFAULT NULL,
  `usuario` varchar(60) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL,
  PRIMARY KEY (`codigo`),
  KEY `idx_devprov_proveedor` (`proveedor`),
  KEY `fk_devprov_almacen` (`almacen`),
  KEY `fk_devprov_factura` (`factura_compra`),
  CONSTRAINT `fk_devprov_almacen` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_devprov_factura` FOREIGN KEY (`factura_compra`) REFERENCES `factura_de_compra` (`codigo`),
  CONSTRAINT `fk_devprov_proveedor` FOREIGN KEY (`proveedor`) REFERENCES `proveedor` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `devolucion_proveedor`
--

LOCK TABLES `devolucion_proveedor` WRITE;
/*!40000 ALTER TABLE `devolucion_proveedor` DISABLE KEYS */;
/*!40000 ALTER TABLE `devolucion_proveedor` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `entrada_inventario`
--

LOCK TABLES `entrada_inventario` WRITE;
/*!40000 ALTER TABLE `entrada_inventario` DISABLE KEYS */;
INSERT INTO `entrada_inventario` VALUES (1,'REC-2',9,NULL,'2026-11-17','2026-11-17 14:09:47','2026-11-17 14:09:47','Entrada por Recepcion de Mercancia #2. prueba','2026-11-17 14:09:47',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(2,'AJ-1',3,NULL,'2026-11-17','2026-11-17 14:10:41','2026-11-17 14:10:41','Entrada por Traslado de Mercancia #1. ','2026-11-17 14:10:41',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(3,'AJ-1',3,NULL,'2026-11-17','2026-11-17 14:13:53','2026-11-17 14:13:53','Entrada por Ajuste de Inventario #1. aumento','2026-11-17 14:13:53',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(4,'AJ-2',3,NULL,'2026-11-17','2026-11-17 14:15:55','2026-11-17 14:15:55','Entrada por Traslado de Mercancia #2. ','2026-11-17 14:15:55',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(5,'REC-3',9,NULL,'2026-11-17','2026-11-17 14:18:02','2026-11-17 14:18:02','Entrada por Recepcion de Mercancia #3. cafe','2026-11-17 14:18:02',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(6,'AJ-3',3,NULL,'2026-11-17','2026-11-17 14:19:10','2026-11-17 14:19:10','Entrada por Traslado de Mercancia #3. cafe','2026-11-17 14:19:10',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL),(7,'REC-4',9,NULL,'2026-11-17','2026-11-17 15:44:19','2026-11-17 15:44:19','Entrada por Recepcion de Mercancia #4. compra a credito','2026-11-17 15:44:19',1,'DOP',_binary '\0',NULL,NULL,NULL,NULL);
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
INSERT INTO `estado_factura` VALUES (1,'PENDIENTE'),(2,'PAGADA');
/*!40000 ALTER TABLE `estado_factura` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `factura_de_compra`
--

DROP TABLE IF EXISTS `factura_de_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factura_de_compra` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `fecha` date DEFAULT NULL,
  `fecha_vencimiento` date DEFAULT NULL,
  `recepcion_mercancia` int DEFAULT NULL,
  `ncf` varchar(25) DEFAULT NULL,
  `numero_factura` varchar(25) DEFAULT NULL,
  `orden_compra` int DEFAULT NULL,
  `condicion_pago` int DEFAULT NULL,
  `tipo_compra` int NOT NULL,
  `proveedor` int DEFAULT NULL,
  `nombre_proveedor` varchar(120) DEFAULT NULL,
  `concepto` text,
  `usuario` int DEFAULT NULL,
  `nombre_usaurio` varchar(85) DEFAULT NULL,
  `sub_total` decimal(12,2) DEFAULT NULL,
  `total_itbis` decimal(12,2) NOT NULL,
  `total_isr` decimal(12,2) NOT NULL,
  `otros_impuesto` decimal(12,2) DEFAULT NULL,
  `total_impuesto` decimal(12,2) DEFAULT NULL,
  `total` decimal(12,2) DEFAULT NULL,
  `total_a_pagar` decimal(12,2) DEFAULT NULL,
  `total_pagado` decimal(12,2) DEFAULT NULL,
  `total_pendiente` decimal(12,2) DEFAULT NULL,
  `anulada` bit(1) DEFAULT NULL,
  `fecha_anulada` datetime DEFAULT NULL,
  `numero` varchar(25) DEFAULT NULL,
  `secuencia_documento` int DEFAULT NULL,
  `estado` int DEFAULT NULL,
  `nombre_estado` varchar(10) DEFAULT NULL,
  `fecha_actualizacion` datetime DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_factura_suplidor` (`proveedor`) USING BTREE,
  KEY `fk_factura_proveedor_usuario` (`usuario`) USING BTREE,
  KEY `fk_factura_suplidor_tipo_compra` (`tipo_compra`) USING BTREE,
  KEY `fk_factura_comp_estado_idx` (`estado`),
  KEY `fk_factura_comp_recepcion_idx` (`recepcion_mercancia`),
  KEY `fk_factura_comp_cond_pago_idx` (`condicion_pago`),
  CONSTRAINT `fk_factura_comp_cond_pago` FOREIGN KEY (`condicion_pago`) REFERENCES `condicion_pago` (`codigo`),
  CONSTRAINT `fk_factura_comp_estado` FOREIGN KEY (`estado`) REFERENCES `estado_factura` (`codigo`),
  CONSTRAINT `fk_factura_comp_recepcion` FOREIGN KEY (`recepcion_mercancia`) REFERENCES `recepcion_mercancia` (`codigo`),
  CONSTRAINT `fk_factura_proveedor` FOREIGN KEY (`proveedor`) REFERENCES `proveedor` (`codigo`),
  CONSTRAINT `fk_factura_proveedor_usuario` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`),
  CONSTRAINT `fk_factura_suplidor_tipo_compra` FOREIGN KEY (`tipo_compra`) REFERENCES `tipo_compra` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `factura_de_compra`
--

LOCK TABLES `factura_de_compra` WRITE;
/*!40000 ALTER TABLE `factura_de_compra` DISABLE KEYS */;
INSERT INTO `factura_de_compra` VALUES (2,'2026-11-17','2026-11-17',2,'20261117','REC-INF-1794924586551',NULL,1,2,10,'TeÃ³filo Antonio RodrÃ­guez','Factura por recepcion de inv no.2',1,'Admin',500.00,90.00,0.00,NULL,NULL,590.00,NULL,590.00,0.00,_binary '\0',NULL,NULL,NULL,2,'PAGADA','2026-11-17 10:09:47'),(3,'2026-11-17','2026-11-17',3,'202611171','202611171',NULL,1,1,385,'MAXIMILIANO ALMONTE RODRIGUEZ','Factura por recepcion de inv no.3',1,'Admin',6000.00,1080.00,0.00,NULL,NULL,7080.00,NULL,7080.00,0.00,_binary '\0',NULL,NULL,NULL,2,'PAGADA','2026-11-17 10:18:02'),(4,'2026-11-17','2026-11-17',4,'2021','REC-INF-1794930259358',NULL,1,2,8,'Samuel GrullÃ³n','Factura por recepcion de inv no.4',1,'Admin',150.00,27.00,0.00,NULL,NULL,177.00,NULL,177.00,0.00,_binary '\0',NULL,NULL,NULL,2,'PAGADA','2026-11-17 11:44:19'),(5,'2026-11-17',NULL,NULL,'','5214',NULL,1,1,10,'TeÃ³filo Antonio RodrÃ­guez','compra',1,'Admin',150.00,0.00,0.00,NULL,NULL,150.00,NULL,0.00,150.00,_binary '\0',NULL,NULL,NULL,NULL,'PENDIENTE',NULL);
/*!40000 ALTER TABLE `factura_de_compra` ENABLE KEYS */;
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
  `total_pagado` decimal(12,2) DEFAULT NULL,
  `total_pendiente` decimal(12,2) DEFAULT NULL,
  `total_devuelto` decimal(12,2) NOT NULL DEFAULT '0.00',
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `factura_de_venta`
--

LOCK TABLES `factura_de_venta` WRITE;
/*!40000 ALTER TABLE `factura_de_venta` DISABLE KEYS */;
INSERT INTO `factura_de_venta` VALUES (1,NULL,2,1,NULL,NULL,NULL,NULL,13,'20-31',1,'RAMON ALMONTE','2026-11-17','2026-11-17 14:11:15','2026-11-17 14:22:35',NULL,_binary '\0',NULL,1,'Administrador',NULL,70.00,0.00,0.00,0.00,0.00,70.00,70.00,0.00,0.00,'20-31'),(2,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-11-17','2026-11-17 14:16:22','2026-11-17 14:16:22',NULL,_binary '\0',NULL,1,'Administrador',NULL,70.00,0.00,0.00,0.00,0.00,70.00,70.00,0.00,0.00,'EMPRESA'),(3,NULL,2,2,NULL,NULL,NULL,NULL,14,'MAXIMILIANO ALMONTE RODRIGUEZ',NULL,'N/A','2026-11-17','2026-11-17 14:19:44','2026-11-17 14:25:13',NULL,_binary '\0',NULL,1,'Administrador',NULL,250.00,0.00,0.00,0.00,0.00,250.00,250.00,0.00,0.00,'Las colinas santiago  c/29 #5'),(4,NULL,1,2,NULL,NULL,NULL,NULL,14,'MAXIMILIANO ALMONTE RODRIGUEZ',NULL,'N/A','2026-11-17','2026-11-17 15:43:07','2026-11-17 15:43:07',NULL,_binary '\0',NULL,1,'Administrador',NULL,88.98,0.00,0.00,16.02,0.00,105.00,0.00,105.00,0.00,'Las colinas santiago  c/29 #5'),(5,NULL,1,2,NULL,NULL,NULL,NULL,14,'MAXIMILIANO ALMONTE RODRIGUEZ',NULL,'N/A','2026-11-17','2026-11-17 18:32:11','2026-11-17 18:32:11',NULL,_binary '\0',NULL,1,'Administrador',NULL,105.00,0.00,0.00,0.00,0.00,105.00,0.00,105.00,0.00,'Las colinas santiago  c/29 #5'),(6,NULL,2,1,NULL,NULL,NULL,NULL,13,'PUBLICO GENERAL',NULL,'N/A','2026-11-17','2026-11-17 21:53:19','2026-11-17 21:53:19',NULL,_binary '\0',NULL,1,'Administrador',NULL,35.00,0.00,0.00,0.00,0.00,35.00,35.00,0.00,0.00,'EMPRESA');
/*!40000 ALTER TABLE `factura_de_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'20261112','Esquema base (dump + scripts sql/ hasta 2026-11-12)','BASELINE','Esquema base (dump + scripts sql/ hasta 2026-11-12)',NULL,'root','2026-11-17 21:47:36',0,1),(2,'20261117','montos decimal','SQL','V20261117__montos_decimal.sql',-1241807634,'root','2026-11-17 21:47:37',371,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historial_limite_credito`
--

DROP TABLE IF EXISTS `historial_limite_credito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historial_limite_credito` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `cliente` int NOT NULL,
  `limite_anterior` decimal(12,2) DEFAULT NULL,
  `limite_nuevo` decimal(12,2) DEFAULT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `usuario` varchar(100) DEFAULT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`codigo`),
  KEY `idx_hist_limite_cliente` (`cliente`,`fecha`),
  CONSTRAINT `fk_hist_limite_cliente` FOREIGN KEY (`cliente`) REFERENCES `cliente` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historial_limite_credito`
--

LOCK TABLES `historial_limite_credito` WRITE;
/*!40000 ALTER TABLE `historial_limite_credito` DISABLE KEYS */;
/*!40000 ALTER TABLE `historial_limite_credito` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `liquidacion_delivery`
--

DROP TABLE IF EXISTS `liquidacion_delivery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `liquidacion_delivery` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL,
  `delivery` int NOT NULL,
  `nombre_usuario` varchar(100) DEFAULT NULL,
  `efectivo_esperado` decimal(18,2) NOT NULL DEFAULT '0.00',
  `efectivo_recibido` decimal(18,2) NOT NULL DEFAULT '0.00',
  `transferencia_recibida` decimal(18,2) NOT NULL DEFAULT '0.00',
  `referencia_transferencia` varchar(100) DEFAULT NULL,
  `diferencia` decimal(18,2) NOT NULL DEFAULT '0.00',
  `faltante_cargado` decimal(18,2) NOT NULL DEFAULT '0.00',
  `total_fiado` decimal(18,2) NOT NULL DEFAULT '0.00',
  `total_liquidado` decimal(18,2) NOT NULL DEFAULT '0.00',
  `observacion` varchar(500) DEFAULT NULL,
  `anulada` tinyint(1) NOT NULL DEFAULT '0',
  `fecha_anulacion` datetime DEFAULT NULL,
  `motivo_anulacion` varchar(300) DEFAULT NULL,
  `usuario_anulacion` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `idx_liq_delivery_fecha` (`fecha`),
  KEY `idx_liq_delivery_motorista` (`delivery`),
  CONSTRAINT `fk_liq_delivery_motorista` FOREIGN KEY (`delivery`) REFERENCES `delivery` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `liquidacion_delivery`
--

LOCK TABLES `liquidacion_delivery` WRITE;
/*!40000 ALTER TABLE `liquidacion_delivery` DISABLE KEYS */;
INSERT INTO `liquidacion_delivery` VALUES (1,'2026-11-17 10:22:35',1,'Admin',70.00,70.00,0.00,NULL,0.00,0.00,0.00,70.00,'',0,NULL,NULL,NULL);
/*!40000 ALTER TABLE `liquidacion_delivery` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lote`
--

DROP TABLE IF EXISTS `lote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lote` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `articulo` int NOT NULL,
  `numero_lote` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_vencimiento` date DEFAULT NULL,
  `fecha_entrada` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `costo_unitario` decimal(12,4) DEFAULT NULL COMMENT 'costo por unidad base al entrar',
  `proveedor` int DEFAULT NULL,
  `documento_origen` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Ej: REC-15, ENT-8, INICIAL',
  `activo` bit(1) NOT NULL DEFAULT b'1',
  `creado_por` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  UNIQUE KEY `uk_lote_articulo_numero` (`articulo`,`numero_lote`),
  KEY `idx_lote_articulo_vence` (`articulo`,`fecha_vencimiento`),
  KEY `idx_lote_vence` (`fecha_vencimiento`),
  KEY `fk_lote_proveedor` (`proveedor`),
  CONSTRAINT `fk_lote_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `fk_lote_proveedor` FOREIGN KEY (`proveedor`) REFERENCES `proveedor` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lote`
--

LOCK TABLES `lote` WRITE;
/*!40000 ALTER TABLE `lote` DISABLE KEYS */;
INSERT INTO `lote` VALUES (1,39,'L20261117','2026-11-23','2026-11-17 10:09:47',25.0000,10,'REC-2',_binary '','SISTEMA'),(2,38,'L20261117','2026-12-01','2026-11-17 10:13:53',50.0000,NULL,'ENT-3',_binary '','SISTEMA');
/*!40000 ALTER TABLE `lote` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lote_almacen`
--

DROP TABLE IF EXISTS `lote_almacen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lote_almacen` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `lote` int NOT NULL,
  `almacen` int NOT NULL,
  `existencia` decimal(12,2) NOT NULL DEFAULT '0.00',
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`codigo`),
  UNIQUE KEY `uk_lote_almacen` (`lote`,`almacen`),
  KEY `idx_lote_almacen_existencia` (`almacen`,`existencia`),
  CONSTRAINT `fk_lote_almacen_almacen` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_lote_almacen_lote` FOREIGN KEY (`lote`) REFERENCES `lote` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lote_almacen`
--

LOCK TABLES `lote_almacen` WRITE;
/*!40000 ALTER TABLE `lote_almacen` DISABLE KEYS */;
INSERT INTO `lote_almacen` VALUES (1,1,1,0.00,'2026-11-17 14:10:41'),(2,1,2,13.00,'2026-11-17 21:53:18'),(3,2,1,3.00,'2026-11-17 15:44:19'),(4,2,2,8.00,'2026-11-17 15:43:07');
/*!40000 ALTER TABLE `lote_almacen` ENABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merma`
--

LOCK TABLES `merma` WRITE;
/*!40000 ALTER TABLE `merma` DISABLE KEYS */;
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
  `lote` int DEFAULT NULL,
  `existencia_lote_anterior` decimal(12,2) DEFAULT NULL,
  `existencia_lote_nueva` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `idx_mov_inv_articulo_fecha` (`articulo`,`fecha_movimiento` DESC),
  KEY `idx_mov_inv_fecha` (`fecha_movimiento`),
  KEY `idx_mov_inv_documento` (`tipo_documento`,`numero_documento`),
  KEY `fk_mov_inv_tp_mov_idx` (`tipo_movimiento`),
  KEY `fk_mov_inv_almacen_idx` (`almacen`),
  KEY `fk_mov_inv_art_empaque_idx` (`articulo_empaque`),
  KEY `idx_mov_inv_lote` (`lote`),
  CONSTRAINT `fk_mov_inv_almacen` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_mov_inv_art_empaque` FOREIGN KEY (`articulo_empaque`) REFERENCES `articulo_empaque` (`codigo`),
  CONSTRAINT `fk_mov_inv_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_mov_inv_lote` FOREIGN KEY (`lote`) REFERENCES `lote` (`codigo`),
  CONSTRAINT `fk_mov_inv_tp_docu` FOREIGN KEY (`tipo_documento`) REFERENCES `tipo_documento` (`codigo`),
  CONSTRAINT `fk_mov_inv_tp_mov` FOREIGN KEY (`tipo_movimiento`) REFERENCES `tipo_movimiento` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimiento_inventario`
--

LOCK TABLES `movimiento_inventario` WRITE;
/*!40000 ALTER TABLE `movimiento_inventario` DISABLE KEYS */;
INSERT INTO `movimiento_inventario` VALUES (1,39,1,1,1,'ENT-1',20.00,0.00,20.00,'2026-11-17 10:09:47','SISTEMA','Entrada por Recepcion de Mercancia #2. prueba',83,1.00,20.00,500.00,0.00,500.00,1,0.00,20.00),(2,39,1,2,2,'SAL-1',20.00,20.00,0.00,'2026-11-17 10:10:41','SISTEMA','Salida por Traslado de Inventario #1. ',83,1.00,20.00,500.00,0.00,500.00,1,20.00,0.00),(3,39,2,1,1,'ENT-2',20.00,0.00,20.00,'2026-11-17 10:10:41','SISTEMA','Entrada por Traslado de Mercancia #1. ',83,1.00,20.00,500.00,0.00,500.00,1,0.00,20.00),(4,39,2,2,2,'SAL-1',2.00,20.00,18.00,'2026-11-17 10:11:16','Administrador','Salida automÃ¡tica por Factura POS #1',83,1.00,2.00,50.00,0.00,50.00,1,20.00,18.00),(5,38,1,1,1,'ENT-3',10.00,0.00,10.00,'2026-11-17 10:13:53','SISTEMA','Entrada por Ajuste de Inventario #1. aumento',82,1.00,10.00,500.00,0.00,500.00,2,0.00,10.00),(6,38,1,2,2,'SAL-2',10.00,10.00,0.00,'2026-11-17 10:15:55','SISTEMA','Salida por Traslado de Inventario #2. ',82,1.00,10.00,500.00,0.00,500.00,2,10.00,0.00),(7,38,2,1,1,'ENT-4',10.00,0.00,10.00,'2026-11-17 10:15:55','SISTEMA','Entrada por Traslado de Mercancia #2. ',82,1.00,10.00,500.00,0.00,500.00,2,0.00,10.00),(8,38,2,2,2,'SAL-2',1.00,10.00,9.00,'2026-11-17 10:16:22','Administrador','Salida automÃ¡tica por Factura POS #2',82,1.00,1.00,50.00,0.00,50.00,2,10.00,9.00),(9,37,1,1,1,'ENT-5',30.00,0.00,30.00,'2026-11-17 10:18:03','SISTEMA','Entrada por Recepcion de Mercancia #3. cafe',80,1.00,30.00,6000.00,0.00,6000.00,NULL,NULL,NULL),(10,37,1,2,2,'SAL-3',10.00,30.00,20.00,'2026-11-17 10:19:10','SISTEMA','Salida por Traslado de Inventario #3. cafe',80,1.00,10.00,2000.00,0.00,2000.00,NULL,NULL,NULL),(11,37,2,1,1,'ENT-6',10.00,0.00,10.00,'2026-11-17 10:19:11','SISTEMA','Entrada por Traslado de Mercancia #3. cafe',80,1.00,10.00,2000.00,0.00,2000.00,NULL,NULL,NULL),(12,37,2,2,2,'SAL-3',1.00,10.00,9.00,'2026-11-17 10:19:44','Administrador','Salida automÃ¡tica por Factura POS #3',80,1.00,1.00,200.00,0.00,200.00,NULL,NULL,NULL),(13,39,2,2,2,'SAL-7',1.00,18.00,17.00,'2026-11-17 11:43:07','Administrador','Salida automÃ¡tica por Factura POS #4',83,1.00,1.00,25.00,0.00,25.00,1,18.00,17.00),(14,38,2,2,2,'SAL-7',1.00,9.00,8.00,'2026-11-17 11:43:07','Administrador','Salida automÃ¡tica por Factura POS #4',82,1.00,1.00,50.00,0.00,50.00,2,9.00,8.00),(15,38,1,1,1,'ENT-7',3.00,0.00,3.00,'2026-11-17 11:44:19','SISTEMA','Entrada por Recepcion de Mercancia #4. compra a credito',82,1.00,3.00,150.00,0.00,150.00,2,0.00,3.00),(49,39,2,2,2,'SAL-14',3.00,17.00,14.00,'2026-11-17 14:32:12','Administrador','Salida automÃ¡tica por Factura POS #5',83,1.00,3.00,75.00,0.00,75.00,1,17.00,14.00),(50,39,2,2,2,'SAL-15',1.00,14.00,13.00,'2026-11-17 17:53:19','Administrador','Salida automÃ¡tica por Factura POS #6',83,1.00,1.00,25.00,0.00,25.00,1,14.00,13.00);
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimientos_caja`
--

LOCK TABLES `movimientos_caja` WRITE;
/*!40000 ALTER TABLE `movimientos_caja` DISABLE KEYS */;
INSERT INTO `movimientos_caja` VALUES (1,1,'POS_PAGO_PROVEEDOR',-590.00,'Egreso por compra #2 Recibo de Egreso #1','2026-11-17 10:09:47','admin'),(2,1,'POS_VENTA',70.00,'Ingreso por venta #2 Recibo de Ingreso #1','2026-11-17 10:16:22','Administrador'),(3,1,'POS_PAGO_PROVEEDOR',-7080.00,'Egreso por compra #3 Recibo de Egreso #2','2026-11-17 10:18:02','admin'),(4,1,'POS_DELIVERY',70.00,'LiquidaciÃ³n delivery #1 - Motorista: RAMON ALMONTE. Esperado RD$ 70.00, efectivo RD$ 70','2026-11-17 10:22:35','Admin'),(5,1,'POS_COBRO_CXC',250.00,'Cobro a cliente MAXIMILIANO ALMONTE RODRIGUEZ - Recibo de Ingreso #3','2026-11-17 10:25:13','Admin'),(6,1,'POS_PAGO_PROVEEDOR',-177.00,'Egreso por compra #4 Recibo de Egreso #3','2026-11-17 11:44:19','admin'),(11,1,'POS_VENTA',35.00,'Ingreso por venta #6 Recibo de Ingreso #4','2026-11-17 17:53:19','Administrador');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orden_de_compra`
--

LOCK TABLES `orden_de_compra` WRITE;
/*!40000 ALTER TABLE `orden_de_compra` DISABLE KEYS */;
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
  `limite_credito` decimal(12,2) DEFAULT NULL,
  `plazo` int DEFAULT NULL,
  `cuenta_contable` varchar(45) DEFAULT NULL,
  `web` varchar(50) DEFAULT NULL,
  `tipo_suplidor` int DEFAULT NULL,
  `fecha_cracion` datetime DEFAULT NULL,
  `creado_por` varchar(80) DEFAULT NULL,
  `fecha_actualizacion` datetime DEFAULT NULL,
  `habilitado` bit(1) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `FK9CA22D1C963F1D1` (`plazo`) USING BTREE,
  KEY `fk_suplidor_tipo_suplidor` (`tipo_suplidor`) USING BTREE,
  CONSTRAINT `proveedor_ibfk_3` FOREIGN KEY (`plazo`) REFERENCES `plazo` (`codigo`),
  CONSTRAINT `proveedor_ibfk_4` FOREIGN KEY (`tipo_suplidor`) REFERENCES `tipo_suplidor` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=386 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedor`
--

LOCK TABLES `proveedor` WRITE;
/*!40000 ALTER TABLE `proveedor` DISABLE KEYS */;
INSERT INTO `proveedor` VALUES (3,'03400098236','Maximilino Rosario','santiagoGeneral Luperon No.66 Mao, Valverde','809-563-8745','N/A','Miche Malivert','809-572-4721',NULL,NULL,7,NULL,NULL,2,NULL,NULL,NULL,_binary ''),(4,'n/a','Repuestos de Madera Torres EstÃ©vez','Alto de cana, Santiago RodrÃ­guez, RD','809-994-9963','n/a','','',NULL,NULL,9,NULL,NULL,2,NULL,NULL,NULL,_binary ''),(5,'03400050799','REPUESTOS MAO Y/O RAFAEL TRUMAN DOMINGUEZ','Av. Estanilao Reyes No.24 Mao','8095723240','info@repuestosmao.com','','n/a',NULL,NULL,10,NULL,NULL,1,NULL,NULL,NULL,_binary ''),(6,'8095726258','Repestos Usados NIdIo','J Amaro SÃ¡nchez #102, Mao Valverde, Rep. Dom.','8095726258','N/A','N/A','n/a',NULL,NULL,10,NULL,NULL,2,NULL,NULL,NULL,_binary ''),(7,'109011002','Comserva, SRL','Ave. Estanilao Reyes No. 2, Mao, Rep. Dom.','8095723736','N/A','Comserva','N/A',NULL,NULL,6,NULL,NULL,1,NULL,NULL,NULL,_binary ''),(8,'092-0001750-8','Samuel GrullÃ³n','','','','','',NULL,NULL,7,NULL,NULL,2,NULL,NULL,NULL,_binary ''),(9,'04600230702','Clarisol TinÃ©o','CenovÃ­','','','Miche Malivert-Nicolas EstÃ©vez','',NULL,NULL,7,NULL,NULL,2,NULL,NULL,NULL,_binary ''),(10,'04600156642','TeÃ³filo Antonio RodrÃ­guez','','','','','04600156642',NULL,NULL,9,NULL,NULL,2,NULL,NULL,NULL,_binary ''),(11,'101001577','CLARO','','809-220-1111','','','',NULL,NULL,5,NULL,NULL,1,NULL,NULL,NULL,_binary ''),(385,'0340012141','MAXIMILIANO ALMONTE RODRIGUEZ','las colinas',NULL,NULL,NULL,'8092807944',NULL,NULL,5,NULL,NULL,1,'2026-10-19 16:50:22',NULL,'2026-10-19 16:50:22',_binary '\0');
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
  `condicion_pago` int DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_recepcion_proveedor_idx` (`proveedor`),
  KEY `fk_recepcion_tipo_documento_idx` (`tipo_documento`),
  KEY `fk_recepcion_estado_idx` (`estado_documento`),
  KEY `fk_recepcion_usuario_idx` (`usuario`),
  KEY `fk_recepcion_alm_idx` (`almacen`),
  KEY `fk_recepcion_oc_idx` (`orden_compra`),
  KEY `fk_recepcion_condpago_idx` (`condicion_pago`),
  CONSTRAINT `fk_recepcion_alm` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `fk_recepcion_condpago` FOREIGN KEY (`condicion_pago`) REFERENCES `condicion_pago` (`codigo`),
  CONSTRAINT `fk_recepcion_estado` FOREIGN KEY (`estado_documento`) REFERENCES `estado_documento` (`codigo`),
  CONSTRAINT `fk_recepcion_oc` FOREIGN KEY (`orden_compra`) REFERENCES `orden_de_compra` (`codigo`),
  CONSTRAINT `fk_recepcion_proveedor` FOREIGN KEY (`proveedor`) REFERENCES `proveedor` (`codigo`),
  CONSTRAINT `fk_recepcion_tipo_documento` FOREIGN KEY (`tipo_documento`) REFERENCES `tipo_documento` (`codigo`),
  CONSTRAINT `fk_recepcion_usuario` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recepcion_mercancia`
--

LOCK TABLES `recepcion_mercancia` WRITE;
/*!40000 ALTER TABLE `recepcion_mercancia` DISABLE KEYS */;
INSERT INTO `recepcion_mercancia` VALUES (2,'2026-11-17 00:00:00',9,'20261117','RECEPCION DE MERCANCIA',10,'TeÃ³filo Antonio RodrÃ­guez','prueba',500.00,90.00,590.00,2,'Recibido Parcial',1,'Admin',1,NULL,NULL,1),(3,'2026-11-17 00:00:00',9,'202611171','RECEPCION DE MERCANCIA',385,'MAXIMILIANO ALMONTE RODRIGUEZ','cafe',6000.00,1080.00,7080.00,2,'Recibido Parcial',1,'Admin',1,NULL,NULL,1),(4,'2026-11-17 00:00:00',9,'2021','RECEPCION DE MERCANCIA',8,'Samuel GrullÃ³n','compra a credito',150.00,27.00,177.00,2,'Recibido Parcial',1,'Admin',1,NULL,NULL,2);
/*!40000 ALTER TABLE `recepcion_mercancia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recibo_de_egreso`
--

DROP TABLE IF EXISTS `recibo_de_egreso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recibo_de_egreso` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `fecha` date DEFAULT NULL,
  `fecha_creacion` datetime DEFAULT NULL,
  `proveedor` int DEFAULT NULL,
  `nombre_proveedor` varchar(85) DEFAULT NULL,
  `concepto` text,
  `total_pagado` decimal(12,2) DEFAULT NULL,
  `balance_pendiente_antes` decimal(12,2) DEFAULT NULL,
  `balance_pendiente_despues` decimal(12,2) DEFAULT NULL,
  `anulado` tinyint(1) NOT NULL DEFAULT '0',
  `caja_chica` int DEFAULT NULL,
  `numero` int DEFAULT NULL,
  `secuencia_documento` int DEFAULT NULL,
  `imprimir` tinyint(1) NOT NULL,
  `usuario` int DEFAULT NULL,
  `nombre_usuario` varchar(85) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_usuario9` (`usuario`) USING BTREE,
  KEY `FK8852E758B7EB70BC` (`proveedor`) USING BTREE,
  KEY `FK8852E758A522BF30` (`usuario`) USING BTREE,
  KEY `fk_recibo_egreso_secuencia_doc` (`secuencia_documento`),
  CONSTRAINT `fk_recibo_egreso_secuencia_doc` FOREIGN KEY (`secuencia_documento`) REFERENCES `secuencia_documento` (`codigo`),
  CONSTRAINT `recibo_egreso_ibfk_1` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`),
  CONSTRAINT `recibo_egreso_ibfk_2` FOREIGN KEY (`proveedor`) REFERENCES `proveedor` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recibo_de_egreso`
--

LOCK TABLES `recibo_de_egreso` WRITE;
/*!40000 ALTER TABLE `recibo_de_egreso` DISABLE KEYS */;
INSERT INTO `recibo_de_egreso` VALUES (1,'2026-11-17','2026-11-17 10:09:47',10,'TeÃ³filo Antonio RodrÃ­guez','Factura por recepcion de inv no.2',590.00,NULL,NULL,0,1,NULL,NULL,0,1,'admin'),(2,'2026-11-17','2026-11-17 10:18:02',385,'MAXIMILIANO ALMONTE RODRIGUEZ','Factura por recepcion de inv no.3',7080.00,NULL,NULL,0,1,NULL,NULL,0,1,'admin'),(3,'2026-11-17','2026-11-17 11:44:19',8,'Samuel GrullÃ³n','Factura por recepcion de inv no.4',177.00,NULL,NULL,0,1,NULL,NULL,0,1,'admin');
/*!40000 ALTER TABLE `recibo_de_egreso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recibo_de_ingreso`
--

DROP TABLE IF EXISTS `recibo_de_ingreso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recibo_de_ingreso` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `fecha` date DEFAULT NULL,
  `fecha_creacion` datetime DEFAULT NULL,
  `cliente` int DEFAULT NULL,
  `nombre_cliente` varchar(85) DEFAULT NULL,
  `concepto` text,
  `total_pagado` decimal(12,2) DEFAULT NULL,
  `balance_pendiente_antes` decimal(12,2) DEFAULT NULL,
  `balance_pendiente_despues` decimal(12,2) DEFAULT NULL,
  `anulado` tinyint(1) NOT NULL DEFAULT '0',
  `caja_chica` int DEFAULT NULL,
  `numero` int DEFAULT NULL,
  `secuencia_documento` int DEFAULT NULL,
  `imprimir` tinyint(1) NOT NULL,
  `usuario` int DEFAULT NULL,
  `nombre_usuario` varchar(85) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_usuario9` (`usuario`) USING BTREE,
  KEY `FK8852E758B7EB70BC` (`cliente`) USING BTREE,
  KEY `FK8852E758A522BF30` (`usuario`) USING BTREE,
  KEY `fk_recibo_ing_secuencia_doc` (`secuencia_documento`),
  CONSTRAINT `fk_recibo_ing_secuencia_doc` FOREIGN KEY (`secuencia_documento`) REFERENCES `secuencia_documento` (`codigo`),
  CONSTRAINT `recibo_ingreso_ibfk_1` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`),
  CONSTRAINT `recibo_ingreso_ibfk_2` FOREIGN KEY (`cliente`) REFERENCES `cliente` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recibo_de_ingreso`
--

LOCK TABLES `recibo_de_ingreso` WRITE;
/*!40000 ALTER TABLE `recibo_de_ingreso` DISABLE KEYS */;
INSERT INTO `recibo_de_ingreso` VALUES (1,'2026-11-17','2026-11-17 10:16:22',13,'PUBLICO GENERAL','Recibo automÃ¡tica por Factura de venta #2',70.00,70.00,0.00,0,NULL,NULL,NULL,0,1,'Administrador'),(2,'2026-11-17','2026-11-17 10:22:35',13,NULL,'Pago con liquidacion de factura',70.00,NULL,NULL,0,NULL,NULL,NULL,0,NULL,NULL),(3,'2026-11-17','2026-11-17 10:25:13',14,'MAXIMILIANO ALMONTE RODRIGUEZ','pago total',250.00,NULL,NULL,0,1,NULL,NULL,0,NULL,'Admin'),(4,'2026-11-17','2026-11-17 17:53:19',13,'PUBLICO GENERAL','Recibo automÃ¡tica por Factura de venta #6',35.00,35.00,0.00,0,NULL,NULL,NULL,0,1,'Administrador');
/*!40000 ALTER TABLE `recibo_de_ingreso` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `salida_inventario`
--

LOCK TABLES `salida_inventario` WRITE;
/*!40000 ALTER TABLE `salida_inventario` DISABLE KEYS */;
INSERT INTO `salida_inventario` VALUES (1,NULL,NULL,'2026-11-17',NULL,'2026-11-17 14:10:41','Salida por Traslado de Inventario #1. ',NULL,2,'1',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(2,NULL,NULL,'2026-11-17',NULL,'2026-11-17 14:11:15','Salida automÃ¡tica por Factura POS #1','2026-11-17 14:11:15',5,'1',NULL,NULL,0,NULL,1,NULL,'Administrador'),(3,NULL,NULL,'2026-11-17',NULL,'2026-11-17 14:15:55','Salida por Traslado de Inventario #2. ',NULL,2,'2',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(4,NULL,NULL,'2026-11-17',NULL,'2026-11-17 14:16:22','Salida automÃ¡tica por Factura POS #2','2026-11-17 14:16:22',5,'2',NULL,NULL,0,NULL,1,NULL,'Administrador'),(5,NULL,NULL,'2026-11-17',NULL,'2026-11-17 14:19:10','Salida por Traslado de Inventario #3. cafe',NULL,2,'3',NULL,NULL,0,NULL,1,NULL,'SISTEMA'),(6,NULL,NULL,'2026-11-17',NULL,'2026-11-17 14:19:44','Salida automÃ¡tica por Factura POS #3','2026-11-17 14:19:44',5,'3',NULL,NULL,0,NULL,1,NULL,'Administrador'),(7,NULL,NULL,'2026-11-17',NULL,'2026-11-17 15:43:07','Salida automÃ¡tica por Factura POS #4','2026-11-17 15:43:07',5,'4',NULL,NULL,0,NULL,1,NULL,'Administrador'),(14,NULL,NULL,'2026-11-17',NULL,'2026-11-17 18:32:12','Salida automÃ¡tica por Factura POS #5','2026-11-17 18:32:11',5,'5',NULL,NULL,0,NULL,1,NULL,'Administrador'),(15,NULL,NULL,'2026-11-17',NULL,'2026-11-17 21:53:19','Salida automÃ¡tica por Factura POS #6','2026-11-17 21:53:19',5,'6',NULL,NULL,0,NULL,1,NULL,'Administrador');
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_compra`
--

LOCK TABLES `tipo_compra` WRITE;
/*!40000 ALTER TABLE `tipo_compra` DISABLE KEYS */;
INSERT INTO `tipo_compra` VALUES (1,'FORMAL'),(2,'INFORMAL');
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_documento`
--

LOCK TABLES `tipo_documento` WRITE;
/*!40000 ALTER TABLE `tipo_documento` DISABLE KEYS */;
INSERT INTO `tipo_documento` VALUES (1,'ENTRADA DE INVENTARIO',1,'2026-09-16 04:00:00','ADMIN'),(2,'SALIDA DE INVENTARIO',1,'2026-09-16 04:00:00','ADMIN'),(3,'AJUSTE DE INVENTARIO',1,'2026-09-16 04:00:00','ADMIN'),(4,'TRANSFERENCIA DE INVENTARIO',1,'2026-09-18 04:00:00','ADMIN'),(5,'FACTURA DE VENTA',1,'2026-09-18 04:00:00','ADMIN'),(6,'ANULACION  DE  FACTURA',1,'2026-09-22 19:13:09','ADMIN'),(7,'DEVOLUCION DE VENTA',1,'2026-09-22 04:00:00','ADMIN'),(8,'ORDEN COMPRA',1,'2026-09-29 04:00:00','ADMIN'),(9,'RECEPCION DE MERCANCIA',1,'2026-10-02 04:00:00','ADMIN'),(10,'DEVOLUCION A PROVEEDOR',1,'2026-11-12 21:50:43','ADMIN');
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `traslado_inventario`
--

LOCK TABLES `traslado_inventario` WRITE;
/*!40000 ALTER TABLE `traslado_inventario` DISABLE KEYS */;
INSERT INTO `traslado_inventario` VALUES (1,'TR-1794924641224','2026-11-17 10:10:41',NULL,1,2,1,NULL,3,''),(2,'TR-1794924955189','2026-11-17 10:15:55',NULL,1,2,1,NULL,3,''),(3,'TR-1794925150415','2026-11-17 10:19:10',NULL,1,2,1,NULL,3,'cafe');
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

--
-- Table structure for table `vehiculo`
--

DROP TABLE IF EXISTS `vehiculo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehiculo` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(85) NOT NULL,
  `placa` varchar(20) NOT NULL,
  `habilitado` bit(1) NOT NULL DEFAULT b'1',
  `fecha_creacion` datetime DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehiculo`
--

LOCK TABLES `vehiculo` WRITE;
/*!40000 ALTER TABLE `vehiculo` DISABLE KEYS */;
INSERT INTO `vehiculo` VALUES (1,'MOTOR NEGRO YAMAJA MODELO GM','521421',_binary '','2026-10-30 14:19:20'),(2,'PASOLA ELECTRICA YAMAHA','52142',_binary '','2026-10-30 14:37:13');
/*!40000 ALTER TABLE `vehiculo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'db_colmadom'
--
/*!50003 DROP FUNCTION IF EXISTS `BALANCE_PENDIENTE_FACT_COMPRA` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `BALANCE_PENDIENTE_FACT_COMPRA`( fact int ) RETURNS double(10,2)
    READS SQL DATA
BEGIN 

	declare total_fact  double(10,2) ; 
	declare balance_pendiente  double(10,2) ; 
    declare total_pago  double(10,2) ; 
    
    set  total_pago=PAGO_FACT_COMPRA(fact);

   SELECT    total into total_fact   FROM  factura_de_compra 
                        where  anulada=false
                         and codigo=fact ;
                         
set  balance_pendiente=total_fact-total_pago;
    
    RETURN    round(balance_pendiente,2)
    ; 
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `BALANCE_PENDIENTE_FACT_VENTA` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `BALANCE_PENDIENTE_FACT_VENTA`(fact int) RETURNS double(10,2)
    READS SQL DATA
BEGIN
    DECLARE total_fact double(10,2);
    DECLARE total_dev double(10,2);
    DECLARE total_pago double(10,2);

    SET total_pago = PAGO_FACT_VENTA(fact);

    SELECT total, COALESCE(total_devuelto, 0) INTO total_fact, total_dev
    FROM factura_de_venta
    WHERE anulada = false AND codigo = fact;

    RETURN ROUND(total_fact - total_dev - total_pago, 2);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `PAGO_FACT_COMPRA` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `PAGO_FACT_COMPRA`( fact int ) RETURNS double(10,2)
    READS SQL DATA
BEGIN 
    declare total double(10,2) ; 
    set total = ( 
        SELECT ifnull(sum(d.total) ,0) as total 
        FROM recibo_de_egreso r 
        inner join detalle_recibo_de_egreso d on r.codigo=d.recibo_egreso
        where r.anulado=false and d.factura=fact 
    ) ; 
    RETURN total; 
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `PAGO_FACT_VENTA` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `PAGO_FACT_VENTA`( fact int ) RETURNS double(10,2)
    READS SQL DATA
BEGIN 
    declare total double(10,2) ; 
    set total = ( 
        SELECT ifnull(sum(d.total) ,0) as total 
        FROM recibo_de_ingreso r 
        inner join detalle_recibo_de_ingreso d on r.codigo=d.recibo_ingreso 
        where r.anulado=false and d.factura=fact 
    ) ; 
    RETURN total; 
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-18 10:19:56
