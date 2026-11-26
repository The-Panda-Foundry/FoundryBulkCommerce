# Treasury1.0.2 clarification — draft, not sent

Hi Yassou,

We are preparing Foundry: Bulk Commerce, a separate server-side NeoForge1.21.1 addon, for public source and binary release. The exact Treasury1.0.2 version pages on Modrinth and CurseForge currently display MIT, but its neoforge.mods.toml says “All Rights Reserved”, as does your repository's current LICENSE.

Could you confirm the licence applying to numismatics_treasury-1.0.2.jar, SHA256 `fde8264ff0e17792b6160e6c41c6ea0b19e5de1343ddddcdf13b5a0bddba46e2`, and point us to the applicable full licence/copyright notice? Is ARR in that artifact stale metadata, or are the platform listings inaccurate?

Our addon calls Treasury shop/bank APIs and uses a cancellable Mixin hook in ServerActionHandler.tradeShop to delegate Sell All to our verified transaction handler after Treasury's access checks. It changes the loaded handler's control flow, but does not modify/rehost your original JAR, bundle your classes/assets or reproduce its method bodies. Original addon code would be source-available under ARR, subject to required third-party exceptions.

Please confirm whether this independent integration/runtime hook and its distribution are permitted, and any attribution or other conditions. If MIT applies, a written confirmation that it covers this exact historical artifact would resolve the discrepancy. We are not asking to claim ownership of or redistribute Treasury itself.

Thanks — The Panda Foundry

Evidence checked26 November2026:
- https://modrinth.com/mod/numismatics-treasury-auction-house/version/1.0.2 (version ID2OYpwG72)
- https://www.curseforge.com/minecraft/mc-mods/numismatics-treasury/files/8837018
- https://github.com/Yassounou/numismatics_treasury/blob/main/LICENSE

The two platform pages show project-level licence fields on a1.0.2 version page; they are not proven immutable per-artifact grants. The current repository licence may not describe historical terms. Neither side of the inconsistency is silently treated as conclusive.
