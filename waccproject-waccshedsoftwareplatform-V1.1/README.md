# WACCShed:  A *W*ater *a*nd *C*limate *C*hange Water*shed* Platform

Principal WACCShed Platform Developers: Yu Jie (v1.0), Diego Cardoso (v1.1)

Developers of WACCShed Hydrology Module: David Dziubanski and Kristie Franz

Other Platform Collaborators:  William J. Gutowski, Chris R. Rehmann, and Leigh Tesfatsion

Contact/Email: Diego Cardoso (dcardoso at iastate dot com)

----------
Key reference: *An Agent-Based Platform for the Study of Watersheds as Coupled Natural and Human Systems* [1]

**Abstract**: This study describes the architecture and capabilities of WACCShed, an open source agent-based Java platform that permits the systematic study of interactions among hydrology, climate, and strategic human decision-making in a watershed over time. To demonstrate the platform's use and capabilities, an application is presented in accordance with ODD protocol requirements that captures, in simplified form, the structural attributes of the Squaw Creek watershed in central Iowa. Illustrative findings are reported for the sensitivity of farmer and city social welfare outcomes to changes in three key treatment factors: farmer land-allocation decision method, farmer targeted savings, and levee quality effectiveness for the mitigation of city flood damage. 


----------

## Contents
1. Platform Overview
2. Software Architecture
3. Illustration
4. How do I get set up?
5. How to run tests?
6. References

----------

## 1. WACCShed Platform Overview: ##
WACCShed is an agent-based framework that permits the representation and study of watersheds as dynamic coupled natural and human (CNH) systems [1]. As depicted in the figure below, WACCShed permits researchers to undertake systematic computational experiments to explore interactions among hydrology, climate, and human decision-making processes in a watershed over time. A key feature of the platform is its ability to model strategic decision-making among the interacting human participants.

![WACCShedWithCity.png](https://bitbucket.org/repo/M9x5jq/images/3207376983-WACCShedWithCity.png)

The WACCShed Platform  facilitates the balancing of two goals: realism and understanding.  The platform can be used to develop and study a spectrum of watershed models ranging from relatively simple conceptual thought-experiments to detailed empirically-grounded representations. The flexible modular architecture of the platform, developed entirely in Java, eases the transition from one form of modeling to the next.

----------

## 2. WACCShed Software Architecture: ##
The WACCShed Platform is a Java software library that facilitates the systematic study of coupled interactions among hydrological, climate, and human decision-making processes over time.

WACCShed is a modified version of GLOWA-Danubia [3].  The latter platform is a highly complex software framework consisting of over 30 Java packages developed specifically for the study of the Danube River watershed.  The Java packages comprising GLOWA-Danubia have been substantially reduced in number and simplified in form in order to permit the user-friendly implementation of small-scale demonstration models as well as empirically-grounded larger-scale models.

![ModuleComponentDiagram.png](https://bitbucket.org/repo/M9x5jq/images/3339199576-ModuleComponentDiagram.png)

As figure shows above, WACCShed consists of five principal types of software components:

**Configuration**: This component reads configuration files into the system and sets up three configuration classes (SimulationConfiguration, AreaMetaData, and ComponentMetaData).

**Modules**: Each module is run iteratively by invoking the following four methods: ProvideData(), StoreData(), GetData(), and Compute(). Each module has: (i) a table of module-specific watershed area units; (ii) a set of DataTables (to store run-time module parameter values); and (iii) a set of interfaces for communication among different modules.

**LocalLinkAdmin**: This component functions as a database for registering and retrieving the interfaces of all of the modules.

**TimeController**: This component coordinates communication among different modules, and ensures that data exchanges among different modules are consistent.

**Simulation**: This component is the Main class for instantiating each of the other four components. It also coordinates the interactions among the four other components, and it makes sure the simulation is fault safe; that is, it will abort the simulation if any simulation failure occurs.

Key capabilities of the WACCShed Platform (V1.0) are as follows:

* The platform can be easily modified and extended in accordance with user requirements.
* The platform has a simple well-designed TimeController to handle time coordination among different modules.
* The platform permits the modules to be flexibly implemented.
* The platform can be run on a cluster of computers, given appropriate extensions of a small number of Java classes.

----------

## 3. WACCShed Illustration: ##

A relatively simple watershed application is presented in reference paper [1] to illustrate how the WACCShed Platform can be used to determine the effects of strategic human interactions on private and social welfare outcomes over time.

This illustrative application captures, in highly simplified form, the structural attributes of the Squaw Creek watershed in central Iowa. The application omits institutional arrangements and policies, such as credit systems and crop insurance programs, in order to highlight more clearly the types of risks faced by human watershed participants arising from uncertain physical and economic conditions. The application also restricts attention to a small number of decision-makers in order to identify with care the manner in which their risk-management practices result in an intrinsic dynamic coupling of natural and human systems.

The application consists of a farmer who owns and manages upstream farmland and a city manager who oversees a downstream city susceptible to flooding.  The principal focus of attention is the dynamic interplay among strategic goal-directed human decision-making, crop production, and hydrological processes. The farmer annually allocates her farmland among cropland, water-retention land, and fallow land in pursuit of consumption and savings objectives.  The city manager annually allocates the city budget among city social services, water-retention land subsidy payments, and levee investments in pursuit of city social welfare objectives.

The land and budget allocation decisions of the farmer and city manager are complicated by environmental uncertainty regarding precipitation patterns, production costs, and crop prices, and by behavioral uncertainty regarding the future decisions of the other agent. Strategic interaction arises because the farmer's land allocations depend on the subsidy rates for retention land set by the city manager, and the city manager considers this dependence when determining these subsidy rates.

Attention is focused of the extent to which farmer and city manager welfare outcomes are aligned under variously tested situations, a critical prerequisite for the effective governance of the watershed system.

----------

## 4. How do I get set up? ##
*(For version 1.0. Instructions for v1.1 coming soon.)*

* ### Summary of set up ###

  The system can be installed within **Windows system** or **Linux System**. Following requirement are for Windows systems. 

**1)** Install java runtime environment (test version: jre1.8.1_31). 

**2)** Install Eclipse (test version: Luna Service Release 2 (4.4.2)) and Mysql (test version: MySQL Community Server (GPL) 5.6.21). 

**3)** At eclipse, clone the repository and import the required jar library within folder "WACCShedSoftwarePlatform/lib". 

**4)** Create your own databases and database files in Mysql. The platform will connect to database stored in Mysql Server, and store and fetch data from specified tables.

* ### Configuration ###

**  *Configure Mysql database and datatables* **  
 In this platform, we created a database "WACC_DataBase", this database stored following information:

  **1** Hourly Ames historical precipitation data of three years are stored (1999, 2007, 2005), the datatable names are "lowPrecipScenario","normalPrecipScenario","highPrecipScenario" respectively. Each data item has four elements (id, site_name, time, hourly_precip). the historical Ames historical precipitation data from 1997-2013 is in "WACCShedSoftwarePlatform/res/data/hydrology/AMW.txt".

  **2** City data table (cityData_1997_2013) and Farmer data table (farmerData_1997_2013). each city data table item has ten elements (id,time,budget,levee_invest,subsidy_rate,levee_quality,flood_damage,road_repair_invest,social_benefit,max_Q), each farmer data table item has eighteen elements (id,time,total_land,buf_area,crop_area,fallow_area,subsidy_rate,corn_price_per_bushel,bushels_per_acre,grow_precip,cropProfit_acre,production_cost_per_acre,total_profits,money_balance,CropCN,current_farmerland_CN,cornConsumption,utilityOfConsumption);

**To get this software platform running, You first needs to create your own database and datatable as above in Mysql (if you don't use mysql, you can choose other database server, but you still need to do above configurations).**

Another option to setup your database system, is to mysqldump the sql files in following locations into your mysql server. "WACCShedSoftwarePlatform/sqlScript/wacc-database.sql". this sql file include all datatables that are being used in the platform. 

This software platform has used mysql operations in following places:

**1)** WACCShedSoftwarePlatform/src/org/components/atmosphere/Atmosphere.java.

**2)** WACCShedSoftwarePlatform/src/org/components/city/CityType1.java.

**3)** WACCShedSoftwarePlatform/src/org/components/farmer/ProductiveFarmer.java.

**4)** WACCShedSoftwarePlatform/src/org/simulation/SimulationServerManager.java.

**NOTE**: You can change your choice of database server, you can also change the table name and table data elements, but just make sure you change the database java library and mysql java code correspondingly in the source code.

** *Configure other platform maintained parameters* **

In path "WACCShedSoftwarePlatform/res/", there are a set of maintained parameters that are configured. The maintained parameters can be divided into two main types: 

**1 Simulation meta data configurations **

**Locations**: "WACCShedSoftwarePlatform/res/configuration"; "WACCShedSoftwarePlatform/res/metadata"

configuration.properties defines the simulation configuration path name. "WACCShedSoftwarePlatform/res/configuration/simulation/1.properties" defines the simulation start time and end time, simulation area's metadata file name as well as different simulation modules' metadata file name. 

In "WACCShedSoftwarePlatform/res/metadata", the "WACCShedSoftwarePlatform/res/metadata/AreaMetaDataConfigDir/AreaMetaData1.properties" defines a number of settings for the simulation land. In "WACCShedSoftwarePlatform/res/metadata/ComponentMetaDataConfigDir/", there are five .properties files defines the settings for five modules in our simulation platform respectively.

This software platform reads the above configurations in following places:

**1)** WACCShedSoftwarePlatform/src/org/configuration/ConfigurationReaderWriter.java

**2)** WACCShedSoftwarePlatform/src/org/metadata/MetaDataAdmin.java

**2 Simulation Module related data configurations **

**Locations**: "WACCShedSoftwarePlatform/res/data"

When you read our paper [1], you will see we used 31 different scenario paths to test our proposed schemes. There are 31 different precipitation path pattern (each pattern lasts 20 years long), located in "WACCShedSoftwarePlatform/res/data/atmosphere/atmosphereScenarios". There are 31 different scenarios paths for corn price as well as input cost located in "WACCShedSoftwarePlatform/res/data/economic/economicScenarios/cornPriceScenarios" and "WACCShedSoftwarePlatform/res/data/economic/economicScenarios/inputCostScenarios" respectively.

 files in "WACCShedSoftwarePlatform/res/data/city", "WACCShedSoftwarePlatform/res/data/farmer" and "WACCShedSoftwarePlatform/res/data/hydrology" contains the settings needed to configure city, farmer and hydrology module, respectively.

This software platform reads the above configurations in following places:

**1)** "WACCShedSoftwarePlatform/src/org/components/atmosphere/Atmosphere.java"

**2)** "WACCShedSoftwarePlatform/src/org/components/city/CityModel.java"

**3)** "WACCShedSoftwarePlatform/src/org/components/city/CityType1.java"

**4)** "WACCShedSoftwarePlatform/src/org/components/economic/Economic.java"

**5)** "WACCShedSoftwarePlatform/src/org/components/farmer/FarmerModel.java"

**6)** "WACCShedSoftwarePlatform/src/org/components/farmer/ProductiveFarmer.java"

**7)** "WACCShedSoftwarePlatform/src/org/components/hydrology/Hydrology.java"

**3 Simulation intermediate result as data configuration **

**Locations**: "WACCShedSoftwarePlatform/res/data/city/farmerON" and "WACCShedSoftwarePlatform/res/data/city/farmerOFF".

In our paper [1], we test city manager and farmer's performance when city manager is ON as well as city manager is OFF, and it is explained in [1] that when city manager is OFF, city manager will set its decision variables as a constant value though out all scenarios and all years. Therefore, when CM is OFF, CM will read its decision variables value setting in "WACCShedSoftwarePlatform/res/data/city/farmerON" or "WACCShedSoftwarePlatform/res/data/city/farmerOFF", subject to whether farmer is ON (two stage decision mode) or farmer is OFF (Max Yearly Expected UOC Decision Mode). 

This software platform reads the above configurations in following places:
**1)** "WACCShedSoftwarePlatform/src/org/components/city/CityType1.java"
 within function *public CityType1(String cityName, String subBasinName, int numOfFarmer, String[] farmerID)*

----------

## 5. How to run tests? ##
*(For version 1.0)*

The main entrance file in this platform is "WACCShedSoftwarePlatform/src/org/simulation/SimulationServerManager.java". You can run batch simulations by modifying the code in main(String[] args) function. Currently, the codes within main(String[] args) are written to run batch simulations and the simulated results will be exported out in csv files. ** You can change the code in this function to suit your requirements **

To replicate the simulation results reported in our paper [1], you just maintain whatever is there in the repository (Currently, it is set up as **CM-ON**), and you needs to change the selected farmer decision algorithms (either simple rule based (two-stage decision mode) or yearly maximization decision process (Max Yearly Expected UOC Decision Mode) ) to get both simulation results. The places in the platform you needs to change is 

**1)** "WACCShedSoftwarePlatform/src/org/components/farmer/ProductiveFarmer.java", in function *public void calc_land_division()*

**2)** when you changed the selected farmer decision functions in **1)**, make sure you selected the right farmer decision functions in "WACCShedSoftwarePlatform/src/org/components/city/CityType1.java", in function  *private void la_decisionSelection()*. Since we assume city manager knows what farmer is thinking.

To set the farmer's risk tolerance vale D (var_D in code) as what you want, go to 

**1)** "WACCShedSoftwarePlatform/src/org/components/farmer/ProductiveFarmer.java", var_D at line 71

**2)** "WACCShedSoftwarePlatform/src/org/components/city/CityType1.java", var_D at line 66

What's more, you also needs to change the data export paths in "WACCShedSoftwarePlatform/src/org/simulation/SimulationServerManager.java", line 73 "exportDataFile" to paths that located in your machine.

===========================================================

To run simulation result where **CM-OFF**, you need comment some blocks of codes as well as uncomment some blocks of codes in the platform. The places in the platform you needs to change is:

**1)** "WACCShedSoftwarePlatform/src/org/simulation/SimulationServerManager.java" line 70 to 73. 

**2)** "WACCShedSoftwarePlatform/src/org/components/city/CityType1.java line 188 to 233. line 689-713

And then you also needs to change the selected farmer decision algorithms (either simple rule based (two-stage decision mode) or yearly maximization decision process (Max Yearly Expected UOC Decision Mode) ) to get both simulation results when CM is OFF.

----------

## 6. References ##
[1] Leigh Tesfatsion, Chris R. Rehmann, Diego S. Cardoso, Yu Jie, and William J. Gutowski, "An Agent-Based Platform for the Study of Watersheds as Coupled Natural and Human Systems," Environmental Modelling & Software, Vol. 89, March, 2017, 40-60. http://dx.doi.org/10.1016/j.envsoft.2016.11.021.  Preprint available at:
http://www2.econ.iastate.edu/tesfatsi/WACCShedPlatform.RevisedWP15022.pdf

[2] Leigh Tesfatsion, Yu Jie, Chris R. Rehmann, and William J. Gutowski,  "WACCShed: A platform for the study of watersheds as dynamic coupled natural and human systems," Economics Department Working Paper No. 1522, Iowa State University, Ames, IA, USA, February 23, 2016. Available at:
http://www2.econ.iastate.edu/tesfatsi/WACCShedPaper.WPVersion.pdf

[3]  GLOWA-Danubia Project, 2014.  German Ministry of Education and Research,http://www.glowa-danube.de/de/opendanubia/allgemein.php