DROP database IF exists `WACC_DataBase`;
CREATE database `WACC_DataBase`;
use `WACC_DataBase`;

DROP TABLE IF EXISTS `lowPrecipScenario`;
CREATE TABLE `lowPrecipScenario` (
  `id` varchar(20) DEFAULT NULL,
  `site_name` varchar(20) DEFAULT NULL,
  `time` datetime DEFAULT '0001-01-01 00:00:00' NOT NULL,
  `hourly_precip` double DEFAULT NULL,
  PRIMARY KEY (`time`)
);

DROP TABLE IF EXISTS `normalPrecipScenario`;
CREATE TABLE `normalPrecipScenario` (
  `id` varchar(20) DEFAULT NULL,
  `site_name` varchar(20) DEFAULT NULL,
  `time` datetime DEFAULT '0001-01-01 00:00:00' NOT NULL,
  `hourly_precip` double DEFAULT NULL,
  PRIMARY KEY (`time`)
);

DROP TABLE IF EXISTS `highPrecipScenario`;
CREATE TABLE `highPrecipScenario` (
  `id` varchar(20) NOT NULL,
  `site_name` varchar(20) DEFAULT NULL,
  `time` datetime DEFAULT '0001-01-01 00:00:00' NOT NULL,
  `hourly_precip` double DEFAULT NULL,
  PRIMARY KEY (`time`)
);

DROP TABLE IF EXISTS `precip1997_2013`;
CREATE TABLE `precip1997_2013` (
  `id` varchar(20) DEFAULT NULL,
  `site_name` varchar(20) DEFAULT NULL,
  `time` datetime DEFAULT '0001-01-01 00:00:00' NOT NULL,
  `hourly_precip` double DEFAULT NULL
);

DROP TABLE IF EXISTS `precip_discharge1973_2013` ;
CREATE TABLE `precip_discharge1973_2013` (
  `time` datetime DEFAULT '0001-01-01 00:00:00' NOT NULL,
  `year_Precip` double DEFAULT NULL,
  `max_Discharge` double DEFAULT NULL,
  `growPrecip` double DEFAULT NULL,
  PRIMARY KEY (`time`)
);



CREATE TABLE `simulations` (
  `sim_id` integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `sim_time` datetime NOT NULL,
  `sim_scenario` integer NOT NULL,
  `sim_theta` double NOT NULL,
  `sim_lqe` double NOT NULL,
  `sim_d` double NOT NULL,
  `sim_cm_on` boolean NOT NULL,
  `sim_f_on` boolean NOT NULL
  );

DROP TABLE IF EXISTS `cityData_1997_2013`;
CREATE TABLE `cityData_1997_2013` (
  `cityData_id` integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `city_id` varchar(20) DEFAULT NULL,
  `cityData_time` datetime NOT NULL,
  `budget` double DEFAULT NULL,
  `levee_invest` double DEFAULT NULL,
  `subsidy_rate` double DEFAULT NULL,
  `levee_quality` double DEFAULT NULL,
  `flood_damage` double DEFAULT NULL,
  `road_repair_invest` double DEFAULT NULL,
  `social_benefit` double DEFAULT NULL,
  `max_Q` double DEFAULT NULL,
  `sim_id` INTEGER NOT NULL,
);

DROP TABLE IF EXISTS `farmerData_1997_2013`;
CREATE TABLE `farmerData_1997_2013` (
  `farmerData_id` integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `farmer_id` varchar(20) DEFAULT NULL,
  `farmerData_time` datetime  NOT NULL,
  `total_land` double DEFAULT NULL,
  `buf_area` double DEFAULT NULL,
  `crop_area` double DEFAULT NULL,
  `fallow_area` double DEFAULT NULL,
  `subsidy_rate` double DEFAULT NULL,
  `corn_price_per_bushel` double DEFAULT NULL,
  `bushels_per_acre` double DEFAULT NULL,
  `grow_precip` double DEFAULT NULL,
  `cropProfit_acre` double DEFAULT NULL,
  `production_cost_per_acre` double DEFAULT NULL,
  `total_profits` double DEFAULT NULL,
  `money_balance` double DEFAULT NULL,
  `CropCN` double DEFAULT NULL,
  `current_farmerland_CN` double DEFAULT NULL,
  `cornConsumption` double DEFAULT NULL,
  `utilityOfConsumption` double DEFAULT NULL,
  `sim_id` INTEGER NOT NULL,
);

-- SHUTDOWN;