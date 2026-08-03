## the codebase
rm -f codebase.txt && ( tree -I '.git|.idea|.gradle|android|ios|assets|documents|node_modules|dist|build|captures|.externalNativeBuild|.cxx|.expo|*.iml|.DS_Store|package-lock.json|codebase.txt|tree.txt|gradle|release|keystore.jks' && find .   \(     -name .git -o     -name .idea -o     -name .gradle -o     -name gradle -o     -name android -o     -name ios -o     -name assets -o     -name documents -o     -name node_modules -o     -name dist -o     -name build -o     -name captures -o     -name .externalNativeBuild -o     -name .cxx -o     -path "./app/release"   \) -prune -o   -type f   ! -name "codebase.txt"   ! -name "tree.txt"   ! -name "package-lock.json"   ! -name "local.properties"   ! -name "*.iml"   ! -name ".DS_Store"   ! -name "*.png"   ! -name "*.jpg"   ! -name "*.jpeg"   ! -name "*.gif"   ! -name "*.webp"   ! -name "*.svg"   ! -name "*.ttf"   ! -name "*.otf"   ! -name "*.woff"   ! -name "*.woff2"   ! -name "*.log"   ! -path "./app/keystore.jks"   -print | while IFS= read -r file; do   echo -e "\n============================================================";   echo "FILE: $file";   echo "============================================================";   cat "$file"; done; ) > codebase.txt

## Todos
[x] projects, transactions work using local storage.
[] push the code into github

[] we can add project, transactions balance correct

[] we have loans as well 
[] add the transactions 
[] calculation are correct 

[] fix the message UI 
[] generate pdf reports 
[]
[]