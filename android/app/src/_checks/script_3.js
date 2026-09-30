
// Optional agentic backend. Keep secrets server-side; endpoint may return structured actions.
window.ATLAS_AGENT_CONFIG=window.ATLAS_AGENT_CONFIG||{endpoint:''};

// ATLAS LINE integration. The Messaging API Channel Secret must remain server-side.
function atlasLineShare(text){
  const value=encodeURIComponent(String(text||'').slice(0,5000));
  const url='https://line.me/R/msg/text/?'+value;
  try { window.open(url,'_blank','noopener,noreferrer'); } catch(e) { location.href=url; }
}
function atlasLineStatus(){
  const id=window.ATLAS_LINE_CONFIG?.channelId||'';
  return id ? 'LINE configurado' : 'LINE não configurado';
}
