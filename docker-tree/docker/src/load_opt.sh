# Runs all scripts found in /opt/docker
# This enables the removal of files and logic without breaking a docker build

load_opt () {
  # shopt is not a recognized command in the base docker image
  # therefore, the verify_scripts.sh in ../opt is needed
  for script in /opt/docker/*.sh; do
      $script
  done
}
