
const C=(window.ATLAS_CONFIG||{}).firebase;
(async()=>{
  if(C&&C.apiKey){
    try{
      const B='https://www.gstatic.com/firebasejs/11.0.2/';
      const [A,F]=await Promise.all([import(B+'firebase-app.js'),import(B+'firebase-auth.js')]);
      const app=A.initializeApp(C),auth=F.getAuth(app);auth.languageCode='pt-BR';await F.setPersistence(auth,F.browserLocalPersistence);
      const provider=new F.GoogleAuthProvider();provider.setCustomParameters({prompt:'select_account'});
      window.FB={
        google:async()=>{if(window.ATLASNative?.googleSignIn){return new Promise((resolve,reject)=>{window.__atlasGoogleResolve=resolve;window.__atlasGoogleReject=reject;try{ATLASNative.googleSignIn()}catch(e){reject(e)}})}return F.signInWithPopup(auth,provider)},
        googleCalendar:async()=>{const p=new F.GoogleAuthProvider();p.addScope('https://www.googleapis.com/auth/calendar');p.setCustomParameters({prompt:'consent'});const r=await F.signInWithPopup(auth,p);const c2=F.GoogleAuthProvider.credentialFromResult(r);if(c2?.accessToken){S.gcalToken=c2.accessToken;save()}return r},
        email:(email,password)=>F.signInWithEmailAndPassword(auth,email,password),signup:(email,password)=>F.createUserWithEmailAndPassword(auth,email,password),profile:(name)=>auth.currentUser?F.updateProfile(auth.currentUser,{displayName:name}):Promise.resolve(),reset:(email)=>F.sendPasswordResetEmail(auth,email),out:()=>F.signOut(auth),del:()=>F.deleteUser(auth.currentUser),user:()=>auth.currentUser,token:async()=>{await F.authStateReady();return auth.currentUser?F.getIdToken(auth.currentUser):''}
      };
      F.onAuthStateChanged(auth,u=>{S.authReady=true;S.authInitializing=false;if(u)finishAuth(u);else if(!S.nativeUser){S.in=0;S.authBusy=false;if(S.v!=='login')S.v='login';draw()}});
      try{const rr=await F.getRedirectResult(auth);if(rr?.user)finishAuth(rr.user)}catch(e){S.authError=authErrorMessage(e,'login')}
      window.dispatchEvent(new Event('fbready'));
    }catch(e){S.authReady=true;S.authInitializing=false;S.authError='Não foi possível inicializar o Firebase Web. O login Google Android usa a autenticação nativa.';window.dispatchEvent(new Event('fbready'));draw()}
  }else{S.authInitializing=false;S.authReady=true;window.dispatchEvent(new Event('fbready'));draw()}
  try{if(window.ATLASNative?.getCurrentGoogleUser){const json=await Promise.resolve(ATLASNative.getCurrentGoogleUser());if(json){const data=typeof json==='string'?JSON.parse(json):json;if(data?.user){S.nativeUser=data.user;finishAuth(data.user)}}}}catch(_){}
})();
