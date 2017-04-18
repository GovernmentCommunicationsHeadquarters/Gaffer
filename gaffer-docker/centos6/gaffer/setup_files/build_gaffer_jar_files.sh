#!/bin/bash

########################################################################
#  Builds jar files based after changes to source code
#  
#######################################################################



# Directory where gaffer source code is cloned to. Directory is removed at the end of the installation process
export INSTALL_DIR=/Gaffer_Source

# Directory where jar files generated by the maven build are transferred to
export TARGET_DIR=/data/Gaffer/Source

# Directory where jar files used to test examples are located
export TEST_DIR=/data/Gaffer/Test

# Directory where gaffer scripts are located
export SCRIPT_DIR=/data/Gaffer/Script

# Directory where dependency jars for gaffer2 are located

export DEPENDENCIES_JAR_DIR=/data/Gaffer/jar_files

export JAR_FILE_DIR=/jar_files

export GAFFER2_DEPENDENCIES=accumulo_mock_dep.tgz

# Copying property file used to set Gaffer graph example file for
# mock accumulo instance

echo "Configuring Gaffer example property file"
cp -pf ${SCRIPT_DIR}/configure_store_prop_dev.sh ${INSTALL_DIR}/.


cd ${INSTALL_DIR}  && ./configure_store_prop_dev.sh

# Remove script used to configure Gaffer accumulo property file

rm -f ${INSTALL_DIR}/configure_store_prop_dev.sh 

echo "Configuration of Gaffer example property file completed. "

# Build jar files using maven
echo "Building jar files from source code"
cd ${INSTALL_DIR}  && /usr/local/maven/bin/mvn clean package -DskipTests 

echo "Maven build completed"  

# Copy dependency files to /data/Gaffer/jar_files

echo  "Copying mock accumulo  dependencies jar files to ${JAR_FILE_DIR} directory"
  
v_file_list=`find  ~/.m2/repository -name  "*.jar" -exec stat --format '%Y :%y %n' "{}" \;| grep -v maven|sort -nr|awk '{ print $5}'` 
for i in ${v_file_list}; do
   echo "Copying $i to ${JAR_FILE_DIR}" 
   cp $i ${JAR_FILE_DIR}/. 
done 

echo "Creating tar file containing mock accumulo dependencies"

cd ${JAR_FILE_DIR} && tar cvzf ${GAFFER2_DEPENDENCIES} *.jar


cp ${JAR_FILE_DIR}/${GAFFER2_DEPENDENCIES} ${DEPENDENCIES_JAR_DIR}/.

echo "Mock Accumulo dependency jar file ${GAFFER2_DEPENDENCIES} created"
cp -pf ~/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.6.2/jackson-annotations-2.6.2.jar ${TARGET_DIR}/.
cp -pf ~/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.6.2/jackson-core-2.6.2.jar ${TARGET_DIR}/.
cp -pf ~/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.6.2/jackson-databind-2.6.2.jar ${TARGET_DIR}/. 
find ${INSTALL_DIR} -name  '*.jar' -type f | xargs cp -t ${TARGET_DIR}/

# Remove test jar files

cd ${TARGET_DIR} && rm -f *-tests.jar 

echo " Removed test jar files"

#  Create a tar file wih dependency jar required to run gaffer example graph
cp -pf ${SCRIPT_DIR}/setupgafferdev.jar ${TARGET_DIR}/.
cd ${TARGET_DIR} && tar cvf gaffer2.tar setupgafferdev.jar
cd ${TARGET_DIR} && tar uvf gaffer2.tar accumulo-store-iterators*.jar
cd ${TARGET_DIR} && tar uvf gaffer2.tar common-util-*.jar 
cd ${TARGET_DIR} && tar uvf gaffer2.tar data-*.jar 
cd ${TARGET_DIR} && tar uvf gaffer2.tar example-*.jar 
cd ${TARGET_DIR} && tar uvf gaffer2.tar function-*.jar 
cd ${TARGET_DIR} && tar uvf gaffer2.tar graph-*.jar 
cd ${TARGET_DIR} && tar uvf gaffer2.tar jackson-*.jar 
cd ${TARGET_DIR} && tar uvf gaffer2.tar simple-operation-library-*.jar 
cd ${TARGET_DIR} && tar uvf gaffer2.tar operation-*.jar 
cd ${TARGET_DIR} && tar uvf gaffer2.tar serialisation-*.jar
cd ${TARGET_DIR} && tar uvf gaffer2.tar simple-function-library-*.jar
cd ${TARGET_DIR} && tar uvf gaffer2.tar simple-serialisation-library-*.jar
cd ${TARGET_DIR} && tar uvf gaffer2.tar store-*.jar
cd ${TARGET_DIR} && tar uvf gaffer2.tar setupgafferdev.jar
cd ${TARGET_DIR} && tar tvf gaffer2.tar

cp ${TARGET_DIR}/gaffer2.tar ${TEST_DIR}/.

echo "Example Graph gaffer2.tar file created and copied to ${TEST_DIR} ."

echo "Extracting jar from file ${GAFFER2_DEPENDENCIES}  containing dependency files required to run"
echo "mock accumulo"


cd ${DEPENDENCIES_JAR_DIR} && tar xvf ${JAR_FILE_DIR}/${GAFFER2_DEPENDENCIES}


echo "Extracting jar from file gaffer2.jar containing jar files required to run"
echo "example Gaffer graph"

cd ${TEST_DIR} && tar xvf gaffer2.tar
 
echo "Jar files extraction completed "
echo "Script completed"
