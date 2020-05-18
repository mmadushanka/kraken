import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SecurityGuard} from 'projects/security/src/lib/security.guard';
import {StorageResolverService} from 'projects/storage/src/lib/storage-resolver.service';

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    canActivate: [SecurityGuard],
    redirectTo: '/workspace',
  },
  {
    path: 'workspace',
    canActivate: [SecurityGuard],
    canLoad: [SecurityGuard],
    resolve: {
      nodes: StorageResolverService
    },
    loadChildren: () => import('./workspace/workspace.module').then(m => m.WorkspaceModule),
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
      // onSameUrlNavigation: 'reload',
      enableTracing: false,
    }
  )],
  exports: [RouterModule],
})
export class AppRoutingModule {
}
