import {main} from "./webapp.js";
import Binds from "./org/ngengine/web/WebBindsHub.js";

Binds.addEventListener("main", async (args)=>{
    return main(args);
});

Binds.fireEvent("ready");
console.log("NGE Worker started");