# Runs all scripts found in /opt/docker
# This enables the removal of files and logic without breaking a docker build
load_opt () {
  shopt -s nullglob
  for script in /opt/docker/*.sh; do
      $script
  done
  shopt -u nullglob
}
