jq -n --arg linklabel 'my label' --arg link 'my link' '{ name: "Download", value: ("[" + $linklabel + "](" + $link + ")"), inline: false }'
