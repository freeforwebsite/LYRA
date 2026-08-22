import os
import re

def replace_in_file(filepath, pattern, replacement, flags=0):
    if not os.path.exists(filepath):
        return
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    new_content = re.sub(pattern, replacement, content, flags=flags)
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)

def main():
    # 1. Update applicationId and references in build files
    for build_file in ['androidApp/build.gradle.kts', 'app/build.gradle.kts']:
        replace_in_file(build_file, r'applicationId\s*=\s*"[^"]+"', 'applicationId = "com.music.lyra"')

    for gs_file in ['androidApp/google-services.json', 'app/google-services.json']:
        replace_in_file(gs_file, r'echo\.music\.iad1tya', 'com.music.lyra')

    # 2. Rebrand App Name in strings
    for strings_file in ['androidApp/src/main/res/values/strings.xml', 'app/src/main/res/values/strings.xml']:
        replace_in_file(strings_file, r'>EchoMusic<', '>LYRA<')
        
    for kt_file in ['common/src/commonMain/kotlin/org/simpmusic/common/Strings.kt', 'core/common/src/commonMain/kotlin/com/maxrave/common/Strings.kt']:
        replace_in_file(kt_file, r'"EchoMusic"', '"LYRA"')

    # 3. Specific UI removals
    main_activity = 'androidApp/src/main/java/echo/music/iad1tya/MainActivity.kt'
    replace_in_file(main_activity, r'AppUpdateDialog\(\)', '')

    # Aggressive search for Support/Donation UI across all Kotlin Multiplatform UI files
    for root_dir in ['composeApp', 'app', 'core']:
        if os.path.exists(root_dir):
            for root, _, files in os.walk(root_dir):
                for file in files:
                    if file.endswith('.kt'):
                        filepath = os.path.join(root, file)
                        # Remove Support/Sponsor sections
                        replace_in_file(filepath, r'(?:Card|Column|Row|item|WelcomeSectionCard)\s*\([^)]*?(?:title|text)\s*=\s*"(?:Support|Sponsor|Donate|Buy me a coffee)[\s\S]*?(?=\n\s*(?:Card|Column|Row|item|WelcomeSectionCard|Spacer|Divider|fun|\}\s*\n))', '')
                        # Change updater repo to the new fork
                        replace_in_file(filepath, r'EchoMusicApp/Echo-Music', 'freeforwebsite/LYRA')
                        replace_in_file(filepath, r'"app-universal-release\.apk"', '"lyra.apk"')
                        replace_in_file(filepath, r'it\.name\s*==\s*"app-universal-release\.apk"', 'it.name == "lyra.apk"')
                        replace_in_file(filepath, r'!it\.name\.contains\("debug",\s*ignoreCase\s*=\s*true\)', 'it.name == "lyra.apk"')

if __name__ == '__main__':
    main()
