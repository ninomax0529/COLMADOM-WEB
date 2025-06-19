CREATE DATABASE  IF NOT EXISTS `db_colmadom_v1` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `db_colmadom_v1`;
-- MySQL dump 10.13  Distrib 8.0.38, for macos14 (arm64)
--
-- Host: 127.0.0.1    Database: db_colmadom_v1
-- ------------------------------------------------------
-- Server version	8.4.2

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `almacen`
--

LOCK TABLES `almacen` WRITE;
/*!40000 ALTER TABLE `almacen` DISABLE KEYS */;
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
  `unidad_entrada` int DEFAULT NULL,
  `unidad_salida` int DEFAULT NULL,
  `existencia` double DEFAULT NULL,
  `maximo` double DEFAULT NULL,
  `minimo` double DEFAULT NULL,
  `apedir` double DEFAULT NULL,
  `precio_compra` double DEFAULT NULL,
  `precio_compra_anterior` double DEFAULT NULL,
  `precio_venta` double DEFAULT NULL,
  `precio_venta_con_itbis` double DEFAULT NULL,
  `precio_venta_anterior` double DEFAULT NULL,
  `ultimo_suplidor` int DEFAULT NULL,
  `exento_itbis` bit(1) NOT NULL DEFAULT b'0',
  `imagen` blob,
  `inventariable` bit(1) NOT NULL,
  `tipo_articulo` int DEFAULT NULL,
  `margen_beneficio` double DEFAULT NULL,
  `porciento_utilidad` double DEFAULT NULL,
  `modelo` varchar(25) DEFAULT NULL,
  `marca` varchar(80) DEFAULT NULL,
  `linea_articulo` int DEFAULT NULL,
  `nombre_linea` varchar(80) DEFAULT NULL,
  `para_venta` bit(1) NOT NULL,
  `para_consumo` bit(1) NOT NULL,
  `ruta_img` varchar(30) DEFAULT NULL,
  `embase` int DEFAULT NULL,
  `nombre_embase` varchar(50) DEFAULT NULL,
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
  PRIMARY KEY (`codigo`),
  KEY `unidad_entrada` (`unidad_entrada`),
  KEY `categoria` (`categoria`),
  KEY `sub_categoria` (`sub_categoria`),
  KEY `tipo_articulo` (`tipo_articulo`),
  KEY `unidad_salida` (`unidad_salida`),
  KEY `fk_articulo_secuecia_doc` (`secuencia_documento`),
  KEY `articulo_unidad_de_venta_FK` (`unidad_de_venta`),
  CONSTRAINT `articulo_unidad_de_venta_FK` FOREIGN KEY (`unidad_de_venta`) REFERENCES `unidad_de_venta` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articulo`
--

LOCK TABLES `articulo` WRITE;
/*!40000 ALTER TABLE `articulo` DISABLE KEYS */;
INSERT INTO `articulo` VALUES (33,0,'SOPITA NOR',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,20,NULL,25,NULL,NULL,NULL,_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,NULL,NULL,_binary '\0',NULL,NULL,_binary '\0',1,NULL,NULL,_binary '\0',NULL,_binary '\0'),(34,0,'yuca blanco',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,10,NULL,14,NULL,NULL,NULL,_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,NULL,NULL,_binary '\0',NULL,NULL,_binary '\0',2,NULL,NULL,_binary '\0',NULL,_binary '\0'),(35,0,'platano verde mao',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,13,NULL,15,NULL,NULL,NULL,_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,NULL,NULL,_binary '\0',NULL,NULL,_binary '\0',1,NULL,NULL,_binary '\0',NULL,_binary '\0'),(36,0,'ZASON RANCHERE DE SOBRE',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,18,NULL,20,NULL,NULL,NULL,_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,NULL,NULL,_binary '\0',NULL,NULL,_binary '\0',1,NULL,NULL,_binary '\0',NULL,_binary '\0'),(37,0,'BERENJENA MORFADA',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,7,NULL,12,NULL,NULL,NULL,_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0',_binary '\0',NULL,NULL,NULL,_binary '\0',NULL,NULL,_binary '\0',2,NULL,NULL,_binary '\0',NULL,_binary '\0');
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
  `nombre_articulo` varchar(50) DEFAULT NULL,
  `almacen` int NOT NULL,
  `nombre_almacen` varchar(50) DEFAULT NULL,
  `existencia` double NOT NULL,
  `unidad` int DEFAULT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT NULL,
  `creado_por` varchar(50) DEFAULT NULL,
  `actualizado_por` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_existencia_articulo` (`articulo`) USING BTREE,
  KEY `fk_existencia_almacen` (`almacen`) USING BTREE,
  KEY `fk_existencia_art_unidad` (`unidad`),
  CONSTRAINT `articulo_almacen_ibfk_1` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`),
  CONSTRAINT `articulo_almacen_ibfk_4` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articulo_almacen`
--

LOCK TABLES `articulo_almacen` WRITE;
/*!40000 ALTER TABLE `articulo_almacen` DISABLE KEYS */;
/*!40000 ALTER TABLE `articulo_almacen` ENABLE KEYS */;
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
  `unidad` int DEFAULT NULL,
  `nombre_unidad` varchar(10) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `cantidad_pedida` double DEFAULT NULL,
  `cantidad_recibida` double DEFAULT NULL,
  `cantidad_pendiente` double DEFAULT NULL,
  `precio_compra` double DEFAULT NULL,
  `precio_compra_anterior` double DEFAULT NULL,
  `costo_unitario` double DEFAULT NULL,
  `orden_compra` int DEFAULT NULL,
  `numero_orden` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `numero_factura` varchar(30) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `suplidor` int DEFAULT NULL,
  `nombre_suplidor` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `existencia_actual` double DEFAULT NULL,
  `nueva_existencia` double DEFAULT NULL,
  `precio_venta_anterior` double DEFAULT NULL,
  `precio_venta` double DEFAULT NULL,
  `almacen` int DEFAULT NULL,
  `nombre_almacen` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_det_entrada_inv_articulo` (`articulo`) USING BTREE,
  KEY `fk_det_entrada_inv_entrada_inv` (`entrada_inventario`) USING BTREE,
  KEY `fk_det_entrada_inv_unidad` (`unidad`) USING BTREE,
  KEY `fk_det_entrada_inv_orden_compra` (`orden_compra`) USING BTREE,
  KEY `detalle_entrada_inventario_ibfk_5` (`almacen`),
  CONSTRAINT `fk_det_ent_inv_articulo` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`),
  CONSTRAINT `fk_det_ent_inv_entrada` FOREIGN KEY (`entrada_inventario`) REFERENCES `entrada_inventario` (`codigo`),
  CONSTRAINT `fk_det_ent_inv_unidad` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`),
  CONSTRAINT `fk_ent_det_inv_almacen` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_entrada_inventario`
--

LOCK TABLES `detalle_entrada_inventario` WRITE;
/*!40000 ALTER TABLE `detalle_entrada_inventario` DISABLE KEYS */;
INSERT INTO `detalle_entrada_inventario` VALUES (39,40,33,'sopita',NULL,'Unidad',0,10,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0,NULL,NULL,NULL,'General'),(40,40,36,'ZASON RANCHERE DE SOBRE',NULL,'Unidad',0,20,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0,NULL,NULL,NULL,'General'),(41,41,35,'platano verde mao',NULL,'Unidad',0,41,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0,NULL,NULL,NULL,'General'),(42,42,33,'SOPITA NOR',NULL,'Unidad',0,15,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0,NULL,NULL,NULL,'General'),(43,42,36,'ZASON RANCHERE DE SOBRE',NULL,'Unidad',0,36,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0,NULL,NULL,NULL,'General');
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
  `precio_compra` double DEFAULT NULL,
  `existencia_actual` double DEFAULT NULL,
  `nueva_existencia` double DEFAULT NULL,
  `almacen` int DEFAULT NULL,
  `nombre_almacen` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `cantidad` double NOT NULL,
  `precio_venta` double DEFAULT NULL,
  `sub_total` double NOT NULL,
  `total_descuento` double NOT NULL,
  `porciento_descuento` double NOT NULL,
  `total_itbis` double NOT NULL,
  `porciento_itbis` double NOT NULL,
  `total` double NOT NULL,
  `numero_de_linea` int NOT NULL,
  PRIMARY KEY (`codigo`),
  KEY `detalle_entrada_inventario_ibfk_5` (`almacen`) USING BTREE,
  KEY `fk_det_entrada_inv_articulo` (`articulo`) USING BTREE,
  KEY `fk_det_entrada_inv_entrada_inv` (`factura`) USING BTREE,
  KEY `fk_det_entrada_inv_unidad` (`unidad`) USING BTREE,
  CONSTRAINT `detalle_factura_de_venta_factura_de_venta_FK` FOREIGN KEY (`factura`) REFERENCES `factura_de_venta` (`codigo`),
  CONSTRAINT `fk_det_ent_inv_articulo_copy` FOREIGN KEY (`articulo`) REFERENCES `articulo` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_det_ent_inv_unidad_copy` FOREIGN KEY (`unidad`) REFERENCES `unidad` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_ent_det_inv_almacen_copy` FOREIGN KEY (`almacen`) REFERENCES `almacen` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_factura_de_venta`
--

LOCK TABLES `detalle_factura_de_venta` WRITE;
/*!40000 ALTER TABLE `detalle_factura_de_venta` DISABLE KEYS */;
INSERT INTO `detalle_factura_de_venta` VALUES (30,16,33,'sopita',NULL,'Unidad',NULL,NULL,NULL,NULL,'General',1,30,30,3,10,4.86,18,31.86,0),(31,16,34,'wqdeqe',NULL,'Unidad',NULL,NULL,NULL,NULL,'General',1,14,14,1.4,10,2.27,18,14.87,0);
/*!40000 ALTER TABLE `detalle_factura_de_venta` ENABLE KEYS */;
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
-- Table structure for table `entrada_inventario`
--

DROP TABLE IF EXISTS `entrada_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `entrada_inventario` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `numero_documento` int DEFAULT NULL,
  `tipo_documento` int DEFAULT NULL,
  `secuencia_documento` int DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT NULL,
  `comentario` text CHARACTER SET latin1 COLLATE latin1_swedish_ci,
  `fecha_contabilizacion` timestamp NULL DEFAULT NULL,
  `moneda` int DEFAULT NULL,
  `nombre_moneda` varchar(100) DEFAULT NULL,
  `tipo_entrada` int DEFAULT NULL,
  `nombre_tipo_entrada` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `entrada_inventario`
--

LOCK TABLES `entrada_inventario` WRITE;
/*!40000 ALTER TABLE `entrada_inventario` DISABLE KEYS */;
INSERT INTO `entrada_inventario` VALUES (40,NULL,NULL,NULL,'2025-06-19','2025-06-19 08:01:24','2025-06-19 08:01:24',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Administrador',NULL),(41,NULL,NULL,NULL,'2025-06-19','2025-06-19 10:36:34','2025-06-19 10:36:34',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Administrador',NULL),(42,NULL,NULL,NULL,'2025-06-19','2025-06-19 11:12:24','2025-06-19 11:12:24',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Administrador',NULL);
/*!40000 ALTER TABLE `entrada_inventario` ENABLE KEYS */;
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
  `tipo_documento` int DEFAULT NULL,
  `secuencia_documento` int DEFAULT NULL,
  `tipo_ncf` int DEFAULT NULL,
  `ncf` varchar(25) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `cliente` varchar(200) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `nombre_cliente` int DEFAULT NULL,
  `tipo_venta` int DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT NULL,
  `fecha_actualizacion` timestamp NULL DEFAULT NULL,
  `comentario` text CHARACTER SET latin1 COLLATE latin1_swedish_ci,
  `anulada` bit(1) DEFAULT NULL,
  `fecha_anulada` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario` int DEFAULT NULL,
  `nombre_usuario` varchar(80) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `anulada_por` varchar(80) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `sub_total` double DEFAULT NULL,
  `total_descuento` double DEFAULT NULL,
  `porciento_descuento` double DEFAULT NULL,
  `total_itbis` double DEFAULT NULL,
  `porciento_itbis` double DEFAULT NULL,
  `total` double DEFAULT NULL,
  `total_abonado` double DEFAULT NULL,
  `total_pendiente` double DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `fk_ent_inv_sec_doc` (`secuencia_documento`) USING BTREE,
  KEY `fk_ent_inv_tipo_doc` (`tipo_documento`) USING BTREE,
  KEY `fk_ent_inv_usuario` (`usuario`) USING BTREE,
  CONSTRAINT `fk_ent_inv_sec_doc_copy_copy` FOREIGN KEY (`secuencia_documento`) REFERENCES `secuencia_documento` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_ent_inv_usuario_copy_copy` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `factura_de_venta`
--

LOCK TABLES `factura_de_venta` WRITE;
/*!40000 ALTER TABLE `factura_de_venta` DISABLE KEYS */;
INSERT INTO `factura_de_venta` VALUES (16,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2025-06-19','2025-06-19 08:02:59','2025-06-19 08:02:59',NULL,NULL,NULL,NULL,'Administrador',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `factura_de_venta` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modulo`
--

LOCK TABLES `modulo` WRITE;
/*!40000 ALTER TABLE `modulo` DISABLE KEYS */;
INSERT INTO `modulo` VALUES (1,'INVENTARIO'),(2,'COMPRA'),(3,'VWNTA');
/*!40000 ALTER TABLE `modulo` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_documento`
--

LOCK TABLES `tipo_documento` WRITE;
/*!40000 ALTER TABLE `tipo_documento` DISABLE KEYS */;
INSERT INTO `tipo_documento` VALUES (1,'ENTRADA DE INVENTARIO',1,'2025-03-26 20:54:17',NULL),(2,'SALIDA DE INVENTARIO',1,'2025-03-26 20:54:17',NULL),(3,'AJUSTE DE INVENTARIO',1,'2025-03-26 20:54:17',NULL),(4,'TRANSFERENCIA DE INVENTARIO',1,'2025-03-26 20:54:17',NULL),(5,'DEVOLUCION DE INVENTARIO',1,'2025-03-26 20:54:17',NULL),(6,'ORDEN DE COMPRA',2,'2025-03-26 20:54:17',NULL),(7,'FACTURA DE COMPRA',2,'2025-03-26 20:54:17',NULL),(8,'OFECTA DE VENTA',3,'2025-03-26 20:54:17',NULL),(9,'ORDEN DE VENTA',3,'2025-03-26 20:54:17',NULL),(10,'FACTURA DE VENTA',3,'2025-03-26 20:54:17',NULL),(11,'NOTA DE CREDITO  CLIENTE',3,'2025-03-26 20:55:55',NULL),(12,'NOTA DE DEBITO CLIENTE',3,'2025-03-26 20:55:55',NULL),(13,'RECIBO DE INGRESO',NULL,'2025-03-26 20:46:19',NULL),(14,'RECIBO DE EGRESO',NULL,'2025-03-26 20:46:19',NULL),(15,'COTIZACION DE COMPRA',2,'2025-03-26 20:54:17',NULL),(16,'NOTA DE CREDITO  SUPLIDOR',2,'2025-03-26 20:56:44',NULL),(17,'ARTICULO',1,'2025-03-26 20:54:17',NULL),(26,'NOTA DE DEBITO  SUPLIDOR',2,'2025-03-26 20:57:17',NULL);
/*!40000 ALTER TABLE `tipo_documento` ENABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unidad`
--

LOCK TABLES `unidad` WRITE;
/*!40000 ALTER TABLE `unidad` DISABLE KEYS */;
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
INSERT INTO `unidad_de_venta` VALUES (1,'UNIDAD'),(2,'BASCULA'),(3,'PAQUETE');
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-20  3:16:56
