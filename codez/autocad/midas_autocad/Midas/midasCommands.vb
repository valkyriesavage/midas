Imports Autodesk.AutoCAD.Runtime
Imports Autodesk.AutoCAD.DatabaseServices
Imports Autodesk.AutoCAD.Geometry

Namespace HorizontalAttributes

    Public Class Commands

        ' Class variable to store the instance of our overrule
        Private Shared myOverrule As MidasOverrule

        <CommandMethod("Midas")>
        Public Shared Sub ImplementOverrule()

            'We only want to create our overrule instance once, 
            ' so we check if it already exists before we create it
            ' (i.e. this may be the 2nd time we've run the command)
            If myOverrule Is Nothing Then
                'Instantiate our overrule class
                myOverrule = New MidasOverrule
                'Register the overrule
                Overrule.AddOverrule(
                  RXClass.GetClass(GetType(AttributeReference)),
                  myOverrule, False)
            End If
            'Make sure overruling is turned on so our overrule works
            Overrule.Overruling = True

        End Sub
    End Class

    'Our custom overrule class derived from TransformOverrule
    Public Class MidasOverrule
        Inherits TransformOverrule

        'We want to change how an AttributeReference responds to being
        ' transformed (moved, rotated, etc.), so we override its
        ' standard TransformBy function.
        Public Overrides Sub TransformBy(ByVal entity As Entity,
                                         ByVal transform As Matrix3d)

            'Call the normal TransformBy function for the attribute
            ' reference we're overruling.
            MyBase.TransformBy(entity, transform)
            'We know entity must be an AttributeReference because 
            ' that is the only entity we registered the overrule for.
            Dim attRef As AttributeReference = entity
            'Set rotation of attribute reference to 0 (horizontal)
            attRef.Rotation = 0.0

        End Sub
    End Class
End Namespace
